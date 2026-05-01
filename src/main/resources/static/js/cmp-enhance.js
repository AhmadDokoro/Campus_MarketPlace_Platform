/* Campus Marketplace — enhancement bootstrapper
   Loads AOS init + a small counter-up utility. */
(function(){
  function initAOS(){
    if (typeof window.AOS !== 'undefined') {
      window.AOS.init({
        duration: 750,
        easing: 'ease-out-cubic',
        once: true,
        offset: 60,
        delay: 0,
        disable: function(){ return window.matchMedia('(prefers-reduced-motion: reduce)').matches; }
      });
    }
  }

  function animateCounters(root){
    var counters = (root || document).querySelectorAll('.cmp-counter');
    counters.forEach(function(counter){
      if (counter.dataset.cmpDone === '1') return;
      var target = parseFloat(counter.getAttribute('data-target')) || 0;
      var duration = parseInt(counter.getAttribute('data-duration')) || 1400;
      var start = 0;
      var startTime = null;
      function step(ts){
        if (!startTime) startTime = ts;
        var progress = Math.min((ts - startTime) / duration, 1);
        var eased = 1 - Math.pow(1 - progress, 3);
        var current = start + (target - start) * eased;
        var decimals = (counter.getAttribute('data-decimals') || '0') | 0;
        counter.textContent = decimals
          ? current.toFixed(decimals)
          : Math.floor(current).toLocaleString();
        if (progress < 1) requestAnimationFrame(step);
        else counter.dataset.cmpDone = '1';
      }
      requestAnimationFrame(step);
    });
  }

  function observeCounters(){
    if (!('IntersectionObserver' in window)) { animateCounters(); return; }
    var obs = new IntersectionObserver(function(entries){
      entries.forEach(function(e){
        if (e.isIntersecting){
          animateCounters(e.target);
          obs.unobserve(e.target);
        }
      });
    }, { threshold: 0.35 });
    document.querySelectorAll('.cmp-counter').forEach(function(el){ obs.observe(el); });
  }

  function ready(fn){
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn);
    else fn();
  }

  ready(function(){
    initAOS();
    observeCounters();
  });
})();
