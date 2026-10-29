// static/js/voz.js
// Dictado de receta: voz -> texto (navegador) -> Java + IA -> campos del formulario
(function () {
  var btn = document.getElementById('btnVoz');
  var estado = document.getElementById('estadoVoz');
  if (!btn) return;

  var Reconocimiento = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Reconocimiento) {
    estado.textContent = 'Tu navegador no soporta dictado. Usa Chrome o Edge.';
    btn.disabled = true;
    return;
  }

  var rec = new Reconocimiento();
  rec.lang = 'es-SV';
  rec.interimResults = false;

  btn.addEventListener('click', function () {
    rec.start();
    btn.classList.add('escuchando');
    estado.textContent = 'Escuchando... habla ahora';
  });

  rec.onresult = async function (e) {
    var texto = e.results[0][0].transcript;
    estado.textContent = 'Entendí: «' + texto + '». Interpretando...';
    try {
      var csrf = document.querySelector('meta[name="_csrf"]').content;
      var header = document.querySelector('meta[name="_csrf_header"]').content;
      var headers = { 'Content-Type': 'application/json' };
      headers[header] = csrf;

      var resp = await fetch('/api/perfiles/' + btn.dataset.perfil + '/voz', {
        method: 'POST', headers: headers, body: JSON.stringify({ texto: texto })
      });
      if (!resp.ok) throw new Error('Error ' + resp.status);
      var datos = await resp.json();

      ['medicamento', 'dosis', 'frecuenciaHoras', 'duracionDias', 'medico'].forEach(function (campo) {
        var v = datos.receta[campo];
        document.getElementById(campo).value = (v === null || v === undefined) ? '' : v;
      });

      estado.textContent = datos.alertas.length
        ? 'ATENCIÓN: ' + datos.alertas.join(' | ')
        : 'Revisa los datos y pulsa «Guardar receta».';
    } catch (err) {
      estado.textContent = 'No se pudo interpretar. Escribe los datos a mano.';
    }
  };

  rec.onend = function () { btn.classList.remove('escuchando'); };
  rec.onerror = function () { estado.textContent = 'No se pudo escuchar. Intenta de nuevo.'; };
})();
