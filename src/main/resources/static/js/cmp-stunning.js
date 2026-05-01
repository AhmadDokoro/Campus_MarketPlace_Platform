/* Campus Marketplace — Stunning JS
   - Auto-injects the aurora host with 4 drifting blobs into <body class="cmp-aurora">
   - Wires cursor-following spotlight on .cmp-spotlight elements
   - Animates .cmp-counter elements when scrolled into view
*/
(function () {
  function injectAurora() {
    if (!document.body.classList.contains('cmp-aurora')) return;
    if (document.querySelector('.cmp-aurora-host')) return;
    var host = document.createElement('div');
    host.className = 'cmp-aurora-host';
    host.setAttribute('aria-hidden', 'true');
    host.innerHTML =
      '<div class="cmp-aurora-blob b1"></div>' +
      '<div class="cmp-aurora-blob b2"></div>' +
      '<div class="cmp-aurora-blob b3"></div>' +
      '<div class="cmp-aurora-blob b4"></div>';
    document.body.insertBefore(host, document.body.firstChild);
  }

  function wireSpotlight() {
    var els = document.querySelectorAll('.cmp-spotlight');
    els.forEach(function (el) {
      el.addEventListener('mousemove', function (e) {
        var r = el.getBoundingClientRect();
        var x = ((e.clientX - r.left) / r.width) * 100;
        var y = ((e.clientY - r.top) / r.height) * 100;
        el.style.setProperty('--mx', x + '%');
        el.style.setProperty('--my', y + '%');
      });
    });
  }

  function animateCounters() {
    var counters = document.querySelectorAll('.cmp-counter');
    if (!counters.length) return;
    var obs = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var el = entry.target;
        if (el.dataset.cmpCounted) return;
        el.dataset.cmpCounted = '1';
        var target = +(el.getAttribute('data-target') || el.textContent || 0);
        var dur = 1400;
        var start = performance.now();
        function tick(now) {
          var p = Math.min(1, (now - start) / dur);
          var eased = 1 - Math.pow(1 - p, 3);
          var v = Math.floor(target * eased);
          el.textContent = v.toLocaleString();
          if (p < 1) requestAnimationFrame(tick);
          else el.textContent = target.toLocaleString();
        }
        requestAnimationFrame(tick);
      });
    }, { threshold: 0.4 });
    counters.forEach(function (c) { obs.observe(c); });
  }

  function dropzonePreview() {
    var zones = document.querySelectorAll('.cmp-dropzone[data-input]');
    zones.forEach(function (zone) {
      var input = document.querySelector(zone.getAttribute('data-input'));
      var preview = zone.querySelector('.cmp-drop-preview');
      if (!input) return;
      ['dragenter', 'dragover'].forEach(function (e) {
        zone.addEventListener(e, function (ev) {
          ev.preventDefault();
          zone.classList.add('dragover');
        });
      });
      ['dragleave', 'drop'].forEach(function (e) {
        zone.addEventListener(e, function (ev) {
          ev.preventDefault();
          zone.classList.remove('dragover');
        });
      });
      zone.addEventListener('drop', function (ev) {
        if (ev.dataTransfer && ev.dataTransfer.files && ev.dataTransfer.files.length) {
          input.files = ev.dataTransfer.files;
          input.dispatchEvent(new Event('change', { bubbles: true }));
        }
      });
      zone.addEventListener('click', function (e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'A' || e.target.tagName === 'BUTTON') return;
        input.click();
      });
      input.addEventListener('change', function () {
        if (!preview) return;
        preview.innerHTML = '';
        Array.from(input.files || []).slice(0, 8).forEach(function (file) {
          if (!file.type.startsWith('image/')) return;
          var url = URL.createObjectURL(file);
          var img = document.createElement('img');
          img.src = url;
          img.alt = file.name;
          img.style.cssText = 'width:80px;height:80px;object-fit:cover;border-radius:10px;margin:4px;box-shadow:0 4px 12px rgba(45,27,105,.18);';
          preview.appendChild(img);
        });
      });
    });
  }

  function init() {
    injectAurora();
    wireSpotlight();
    animateCounters();
    dropzonePreview();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
