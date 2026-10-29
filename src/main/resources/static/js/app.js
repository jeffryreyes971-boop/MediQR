// static/js/app.js · comportamientos comunes de MediQR
document.addEventListener('DOMContentLoaded', function () {

  // 1) Confirmación antes de eliminar: <form data-confirm="¿Seguro?">
  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!window.confirm(form.dataset.confirm)) e.preventDefault();
    });
  });

  // 2) Las alertas verdes y azules se cierran solas a los 6 segundos
  document.querySelectorAll('.alert-success, .alert-info').forEach(function (a) {
    setTimeout(function () { a.style.transition = 'opacity .5s'; a.style.opacity = '0'; }, 6000);
    setTimeout(function () { a.remove(); }, 6600);
  });

  // 3) La pestaña activa se desplaza a la vista en pantallas pequeñas
  var activa = document.querySelector('.mq-tabs .nav-link.active');
  if (activa) activa.scrollIntoView({ inline: 'center', block: 'nearest' });
});
