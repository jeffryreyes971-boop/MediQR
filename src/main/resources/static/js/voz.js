// static/js/voz.js
(function () {
  var btn = document.getElementById('btnVoz');
  var estado = document.getElementById('estadoVoz');
  if (!btn) return;

  var Reconocimiento = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Reconocimiento) {
    if (estado) estado.textContent = 'Tu navegador no soporta dictado. Usa Chrome o Edge.';
    btn.disabled = true;
    return;
  }

  var rec = new Reconocimiento();
  rec.lang = 'es-SV';
  rec.interimResults = true;
  rec.continuous = false;

  var escuchando = false;
  var textoFinal = '';

  btn.addEventListener('click', function () {
    if (escuchando) {
      rec.stop();
      return;
    }

    textoFinal = '';
    try {
      rec.start();
      escuchando = true;
      btn.classList.add('escuchando');
      btn.textContent = 'Detener dictado';
      if (estado) estado.textContent = 'Escuchando... habla ahora.';
    } catch (err) {
      if (estado) estado.textContent = 'Error al activar el micrófono.';
    }
  });

  rec.onresult = function (e) {
    var textoProvisorio = '';
    for (var i = e.resultIndex; i < e.results.length; i++) {
      var transcripcion = e.results[i][0].transcript;
      if (e.results[i].isFinal) {
        textoFinal += transcripcion;
      } else {
        textoProvisorio += transcripcion;
      }
    }
    var textoActual = textoFinal || textoProvisorio;
    if (estado && textoActual) {
      estado.textContent = 'Entendí: «' + textoActual + '»...';
    }
  };

  rec.onerror = function (e) {
    escuchando = false;
    btn.classList.remove('escuchando');
    btn.textContent = 'Dictar receta';
    if (estado) estado.textContent = 'Error al escuchar (' + e.error + '). Intenta de nuevo.';
  };

  rec.onend = async function () {
    escuchando = false;
    btn.classList.remove('escuchando');
    btn.textContent = 'Dictar receta';

    if (!textoFinal.trim()) return;

    if (estado) estado.textContent = 'Interpretando «' + textoFinal + '» con IA...';

    try {
      var csrfMeta = document.querySelector('meta[name="_csrf"]');
      var headerMeta = document.querySelector('meta[name="_csrf_header"]');

      var headers = { 'Content-Type': 'application/json' };

      if (csrfMeta && headerMeta && csrfMeta.content) {
        headers[headerMeta.content] = csrfMeta.content;
      } else {
        var inputCsrf = document.querySelector('input[name="_csrf"]');
        if (inputCsrf) {
          headers['X-CSRF-TOKEN'] = inputCsrf.value;
        }
      }

      var resp = await fetch('/api/perfiles/' + btn.dataset.perfil + '/voz', {
        method: 'POST',
        headers: headers,
        credentials: 'same-origin',
        body: JSON.stringify({ texto: textoFinal })
      });

      if (!resp.ok) throw new Error('Error HTTP ' + resp.status);
      var datos = await resp.json();

      // Muestra en la consola de Developer Tools el JSON exacto retornado
      console.log('Respuesta recibida del servidor:', datos);

      // Extrae la receta sin importar si viene en datos.receta, datos.datos o directamente en datos
      var r = datos.receta || datos.datos || datos;

      if (r) {
        // Asignación con búsqueda amplia de propiedades
        var inputMed = document.getElementById('medicamento');
        var inputDos = document.getElementById('dosis');
        var inputFrec = document.getElementById('frecuenciaHoras');
        var inputDur = document.getElementById('duracionDias');
        var inputDoc = document.getElementById('medico');

        if (inputMed) inputMed.value = r.medicamento || r.nombre || r.nombreMedicamento || '';
        if (inputDos) inputDos.value = r.dosis || r.cantidad || '';
        if (inputFrec) inputFrec.value = r.frecuenciaHoras || r.frecuencia_horas || r.frecuencia || r.cada || '';
        if (inputDur) inputDur.value = r.duracionDias || r.duracion_dias || r.duracion || r.dias || '';
        if (inputDoc) inputDoc.value = r.medico || r.doctor || r.nombreMedico || '';
      }

      if (estado) {
        estado.textContent = (datos.alertas && datos.alertas.length)
          ? 'ATENCIÓN: ' + datos.alertas.join(' | ')
          : 'Revisa los datos y pulsa «Guardar receta».';
      }
    } catch (err) {
      console.error('Error en el dictado:', err);
      if (estado) estado.textContent = 'No se pudo interpretar. Escribe los datos a mano.';
    }
  };
})();
