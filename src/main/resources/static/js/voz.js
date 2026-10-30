// static/js/voz.js
// Dictado de receta: voz -> texto (navegador) -> Java + IA -> campos del formulario
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
  rec.interimResults = true; // Permite ver las palabras en tiempo real mientras hablas
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
      if (estado) estado.textContent = 'Error al activar el micrófono. Intenta de nuevo.';
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

    if (!estado) return;
    if (e.error === 'not-allowed') {
      estado.textContent = 'Permiso denegado. Permite el uso del micrófono en tu navegador.';
    } else if (e.error === 'no-speech') {
      estado.textContent = 'No se detectó voz. Intenta de nuevo hablando más cerca del micrófono.';
    } else {
      estado.textContent = 'Error al escuchar (' + e.error + '). Intenta de nuevo.';
    }
  };

  rec.onend = async function () {
    escuchando = false;
    btn.classList.remove('escuchando');
    btn.textContent = 'Dictar receta';

    if (!textoFinal.trim()) {
      return; // Si no hubo palabras detectadas, finaliza sin llamar al servidor
    }

    if (estado) estado.textContent = 'Interpretando «' + textoFinal + '» con IA...';

    try {
      var csrfMeta = document.querySelector('meta[name="_csrf"]');
      var headerMeta = document.querySelector('meta[name="_csrf_header"]');
      
      var headers = { 'Content-Type': 'application/json' };
      if (csrfMeta && headerMeta) {
        headers[headerMeta.content] = csrfMeta.content;
      }

      var resp = await fetch('/api/perfiles/' + btn.dataset.perfil + '/voz', {
        method: 'POST', 
        headers: headers, 
        body: JSON.stringify({ texto: textoFinal })
      });

      if (!resp.ok) throw new Error('Error ' + resp.status);
      var datos = await resp.json();

      ['medicamento', 'dosis', 'frecuenciaHoras', 'duracionDias', 'medico'].forEach(function (campo) {
        var el = document.getElementById(campo);
        if (el && datos.receta) {
          var v = datos.receta[campo];
          el.value = (v === null || v === undefined) ? '' : v;
        }
      });

      if (estado) {
        estado.textContent = (datos.alertas && datos.alertas.length)
          ? 'ATENCIÓN: ' + datos.alertas.join(' | ')
          : 'Revisa los datos y pulsa «Guardar receta».';
      }
    } catch (err) {
      if (estado) estado.textContent = 'No se pudo interpretar. Escribe los datos a mano.';
    }
  };
})();
