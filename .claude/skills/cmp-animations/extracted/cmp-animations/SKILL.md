---
name: cmp-animations
description: >
  Campus Marketplace Platform animation and micro-interaction skill for Thymeleaf templates.
  Use this skill whenever Kabiru asks for animations, transitions, scroll effects, hover effects,
  loading states, skeleton loaders, page transitions, parallax, fade-in, slide-in, bounce, 
  pulse, shimmer, typing effects, counter animations, or any motion/movement on the Campus 
  Marketplace Platform. Also trigger when asked to "make it alive", "add life", "make it 
  interactive", "make it dynamic", "add wow factor", "smooth transitions", or anything 
  related to visual motion in the project. This skill covers AOS scroll animations, GSAP 
  power animations, CSS keyframes, and Thymeleaf-compatible animation patterns.
---

# Campus Marketplace Platform — Animations & Micro-Interactions

## Overview
This skill transforms the Campus Marketplace from a static site into a LIVING, breathing 
web application. Every animation serves a purpose — guiding the eye, confirming actions, 
and creating delight. All animations are CSS/JS-based and work with server-rendered 
Thymeleaf templates (no React/Vue needed).

## CDN Dependencies — Add to layout `<head>`

```html
<!-- AOS (Animate on Scroll) -->
<link href="https://unpkg.com/aos@2.3.4/dist/aos.css" rel="stylesheet">
<script src="https://unpkg.com/aos@2.3.4/dist/aos.js" defer></script>

<!-- GSAP (GreenSock Animation Platform) — for power animations -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js" defer></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/ScrollTrigger.min.js" defer></script>
```

### Initialize AOS — Add before closing `</body>`
```html
<script>
  document.addEventListener('DOMContentLoaded', function() {
    AOS.init({
      duration: 800,
      easing: 'ease-out-cubic',
      once: true,
      offset: 80,
      delay: 0,
    });
  });
</script>
```

## Animation Categories

---

### 1. Scroll Reveal Animations (AOS)

Use `data-aos` attributes directly on Thymeleaf HTML elements. No JS needed.

#### Standard Reveals
```html
<!-- Fade up (most common — use for cards, sections) -->
<div data-aos="fade-up">...</div>

<!-- Fade up with staggered delay (use for card grids) -->
<div class="row">
  <div class="col" data-aos="fade-up" data-aos-delay="0">Card 1</div>
  <div class="col" data-aos="fade-up" data-aos-delay="100">Card 2</div>
  <div class="col" data-aos="fade-up" data-aos-delay="200">Card 3</div>
  <div class="col" data-aos="fade-up" data-aos-delay="300">Card 4</div>
</div>

<!-- Fade in from sides (use for two-column layouts) -->
<div data-aos="fade-right">Left content</div>
<div data-aos="fade-left">Right content</div>

<!-- Zoom in (use for hero images, featured products) -->
<div data-aos="zoom-in" data-aos-duration="600">Featured</div>

<!-- Flip (use for stats, counters) -->
<div data-aos="flip-up" data-aos-delay="100">Stat card</div>
```

#### Thymeleaf Dynamic Stagger (for product loops)
```html
<div th:each="product, iterStat : ${products}" 
     class="product-card"
     data-aos="fade-up" 
     th:attr="data-aos-delay=${iterStat.index * 100}">
  <!-- Product content -->
</div>
```

---

### 2. CSS Keyframe Animations (Custom)

Add these to your main CSS file. These are reusable across all pages.

```css
/* ===== ENTRANCE ANIMATIONS ===== */

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-40px); }
  to   { opacity: 1; transform: translateX(0); }
}

@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(40px); }
  to   { opacity: 1; transform: translateX(0); }
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.9); }
  to   { opacity: 1; transform: scale(1); }
}

@keyframes slideInFromBottom {
  from { transform: translateY(100%); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
}

/* ===== ATTENTION ANIMATIONS ===== */

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.05); }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 5px rgba(255, 215, 0, 0.3); }
  50%      { box-shadow: 0 0 20px rgba(255, 215, 0, 0.6); }
}

@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-10px); }
}

@keyframes ripple {
  0%   { transform: scale(0); opacity: 0.5; }
  100% { transform: scale(4); opacity: 0; }
}

@keyframes typing {
  from { width: 0; }
  to   { width: 100%; }
}

@keyframes blink {
  0%, 100% { border-color: transparent; }
  50%      { border-color: var(--cmp-gold-primary); }
}

@keyframes countUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ===== LOADING ANIMATIONS ===== */

@keyframes skeletonLoading {
  0%   { background-position: -200px 0; }
  100% { background-position: calc(200px + 100%) 0; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@keyframes bounceIn {
  0%   { transform: scale(0.3); opacity: 0; }
  50%  { transform: scale(1.05); }
  70%  { transform: scale(0.95); }
  100% { transform: scale(1); opacity: 1; }
}
```

### Utility Classes
```css
/* Apply these classes to trigger animations */
.animate-fade-up     { animation: fadeInUp 0.6s var(--ease-default) forwards; }
.animate-fade-down   { animation: fadeInDown 0.6s var(--ease-default) forwards; }
.animate-fade-left   { animation: fadeInLeft 0.6s var(--ease-default) forwards; }
.animate-fade-right  { animation: fadeInRight 0.6s var(--ease-default) forwards; }
.animate-scale-in    { animation: scaleIn 0.5s var(--ease-default) forwards; }
.animate-bounce-in   { animation: bounceIn 0.6s var(--ease-default) forwards; }
.animate-pulse       { animation: pulse 2s ease-in-out infinite; }
.animate-glow        { animation: glow 2s ease-in-out infinite; }
.animate-float       { animation: float 3s ease-in-out infinite; }

/* Stagger delays */
.delay-100 { animation-delay: 100ms; }
.delay-200 { animation-delay: 200ms; }
.delay-300 { animation-delay: 300ms; }
.delay-400 { animation-delay: 400ms; }
.delay-500 { animation-delay: 500ms; }
```

---

### 3. Hover & Micro-Interactions

```css
/* ===== CARD HOVER — Lift + Glow ===== */
.cmp-card-animated {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.cmp-card-animated:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(26, 10, 62, 0.15);
}

/* ===== BUTTON RIPPLE EFFECT ===== */
.btn-ripple {
  position: relative;
  overflow: hidden;
}
.btn-ripple::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}
.btn-ripple:active::after {
  width: 300px;
  height: 300px;
}

/* ===== IMAGE HOVER — Zoom + Overlay ===== */
.img-hover-zoom {
  overflow: hidden;
  border-radius: var(--radius-md);
}
.img-hover-zoom img {
  transition: transform 0.5s var(--ease-default);
}
.img-hover-zoom:hover img {
  transform: scale(1.08);
}

/* ===== LINK UNDERLINE ANIMATION ===== */
.link-animated {
  position: relative;
  text-decoration: none;
}
.link-animated::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--cmp-gold-primary);
  transition: width 0.3s var(--ease-default);
}
.link-animated:hover::after {
  width: 100%;
}

/* ===== NAVBAR LINK GLOW ===== */
.nav-link-glow {
  position: relative;
  transition: color 0.3s ease;
}
.nav-link-glow:hover {
  color: var(--cmp-gold-primary) !important;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
}

/* ===== PRODUCT PRICE TAG BOUNCE ===== */
.price-tag:hover {
  animation: pulse 0.4s ease-in-out;
  color: var(--cmp-gold-primary);
}

/* ===== NOTIFICATION BELL SHAKE ===== */
@keyframes bellShake {
  0%, 100% { transform: rotate(0); }
  15%      { transform: rotate(15deg); }
  30%      { transform: rotate(-15deg); }
  45%      { transform: rotate(10deg); }
  60%      { transform: rotate(-10deg); }
  75%      { transform: rotate(5deg); }
}
.bell-shake:hover {
  animation: bellShake 0.6s ease;
}
```

---

### 4. Skeleton Loading States

```css
/* Skeleton loader — use while products/data loads */
.skeleton {
  background: linear-gradient(90deg, 
    var(--cmp-gray-100) 25%, 
    var(--cmp-gray-200) 50%, 
    var(--cmp-gray-100) 75%
  );
  background-size: 200px 100%;
  animation: skeletonLoading 1.5s infinite;
  border-radius: var(--radius-sm);
}
.skeleton-text   { height: 14px; margin-bottom: 8px; width: 80%; }
.skeleton-title  { height: 20px; margin-bottom: 12px; width: 60%; }
.skeleton-image  { height: 200px; width: 100%; }
.skeleton-avatar { height: 48px; width: 48px; border-radius: 50%; }
.skeleton-button { height: 40px; width: 120px; border-radius: var(--radius-md); }
```

---

### 5. Number Counter Animation (JS)

```html
<script>
function animateCounters() {
  const counters = document.querySelectorAll('.counter');
  counters.forEach(counter => {
    const target = +counter.getAttribute('data-target');
    const duration = 2000;
    const step = target / (duration / 16);
    let current = 0;
    const timer = setInterval(() => {
      current += step;
      if (current >= target) {
        counter.textContent = target.toLocaleString();
        clearInterval(timer);
      } else {
        counter.textContent = Math.floor(current).toLocaleString();
      }
    }, 16);
  });
}

// Trigger when element scrolls into view
const observer = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      animateCounters();
      observer.disconnect();
    }
  });
}, { threshold: 0.5 });

document.addEventListener('DOMContentLoaded', () => {
  const statsSection = document.querySelector('.stats-section');
  if (statsSection) observer.observe(statsSection);
});
</script>
```

Usage in Thymeleaf:
```html
<div class="stats-section" data-aos="fade-up">
  <div class="stat">
    <span class="counter" data-target="500">0</span>
    <p>Active Students</p>
  </div>
</div>
```

---

### 6. Typing Effect for Hero Text

```html
<script>
function typeWriter(element, text, speed = 50) {
  let i = 0;
  element.textContent = '';
  function type() {
    if (i < text.length) {
      element.textContent += text.charAt(i);
      i++;
      setTimeout(type, speed);
    }
  }
  type();
}

document.addEventListener('DOMContentLoaded', () => {
  const heroText = document.querySelector('.hero-typing');
  if (heroText) {
    const text = heroText.getAttribute('data-text');
    typeWriter(heroText, text, 60);
  }
});
</script>
```

---

### 7. Page Transition Effect

```css
/* Smooth page entry */
.page-enter {
  animation: fadeInUp 0.5s var(--ease-default);
}

/* Add to main content wrapper on every page */
main, .main-content {
  animation: fadeInUp 0.4s ease-out;
}
```

---

## Animation Guidelines

1. **Performance**: Use `transform` and `opacity` only — never animate `width`, `height`, `top`, `left`
2. **Timing**: Most animations should be 300-600ms. Longer than 1s feels sluggish.
3. **Easing**: Use `cubic-bezier(0.4, 0, 0.2, 1)` (ease-out) for entrances, `ease-in-out` for loops
4. **Stagger**: When animating lists/grids, stagger by 80-120ms per item
5. **`once: true`**: AOS should only animate once per scroll (already configured in init)
6. **Reduce Motion**: Always respect user preference:
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
  [data-aos] { opacity: 1 !important; transform: none !important; }
}
```
7. **Mobile**: Keep animations simpler on mobile — disable parallax, reduce durations
