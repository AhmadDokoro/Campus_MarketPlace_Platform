---
name: cmp-splash
description: >
  Campus Marketplace Platform splash screen, loading page, and first-impression UI skill.
  Use this skill whenever Kabiru asks about the splash page, splash screen, loading screen, 
  preloader, intro animation, welcome screen, first-time experience, onboarding UI, landing 
  page animation, page loader, progress bar loader, or "what users see first". Also trigger 
  when asked to make the site "wow" on first load, "impress judges", "competition ready", 
  or improve the initial loading experience. This skill covers splash screen design, preloader 
  animations, hero section entrance choreography, and first-impression optimization for the 
  Campus Marketplace Platform.
---

# Campus Marketplace Platform — Splash Screen & First Impression UI

## Overview
The splash page is the FIRST thing judges and users see. It must communicate: 
"This is a professional, polished product." Think of it like the curtain rising 
at a theatre — it builds anticipation, then reveals the stage.

## Splash Screen Implementation

### Full-Page Splash Preloader

This splash screen appears while the page loads, shows the UMT logo with animation, 
then smoothly reveals the actual page.

```html
<!-- Add as FIRST element inside <body> -->
<div id="splash-screen" class="splash-screen">
  <div class="splash-content">
    <!-- Logo animation -->
    <div class="splash-logo">
      <img th:src="@{/images/umt-logo.png}" alt="UMT" class="splash-logo-img">
    </div>
    
    <!-- Animated text -->
    <div class="splash-text">
      <h1 class="splash-title">
        <span class="splash-word" style="--i:0">Campus</span>
        <span class="splash-word splash-highlight" style="--i:1">Marketplace</span>
      </h1>
      <p class="splash-tagline">A trusted campus marketplace built for UMT</p>
    </div>
    
    <!-- Loading bar -->
    <div class="splash-loader">
      <div class="splash-loader-bar"></div>
    </div>
  </div>
  
  <!-- Decorative elements -->
  <div class="splash-particles">
    <div class="particle" style="--x:10%;--y:20%;--delay:0s;--size:4px;"></div>
    <div class="particle" style="--x:80%;--y:30%;--delay:0.5s;--size:6px;"></div>
    <div class="particle" style="--x:30%;--y:70%;--delay:1s;--size:3px;"></div>
    <div class="particle" style="--x:70%;--y:80%;--delay:0.3s;--size:5px;"></div>
    <div class="particle" style="--x:50%;--y:10%;--delay:0.8s;--size:4px;"></div>
    <div class="particle" style="--x:20%;--y:50%;--delay:1.2s;--size:3px;"></div>
    <div class="particle" style="--x:90%;--y:60%;--delay:0.6s;--size:5px;"></div>
    <div class="particle" style="--x:60%;--y:40%;--delay:0.9s;--size:4px;"></div>
  </div>
</div>
```

### Splash Screen CSS

```css
/* ===== SPLASH SCREEN ===== */
.splash-screen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1A0A3E 0%, #2D1B69 40%, #4A2C8A 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  transition: opacity 0.6s ease, visibility 0.6s ease;
  overflow: hidden;
}

.splash-screen.fade-out {
  opacity: 0;
  visibility: hidden;
}

.splash-content {
  text-align: center;
  z-index: 2;
}

/* Logo entrance */
.splash-logo {
  margin-bottom: 24px;
}
.splash-logo-img {
  width: 100px;
  height: auto;
  animation: splashLogoEntry 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) forwards,
             splashLogoFloat 3s ease-in-out 1s infinite;
  opacity: 0;
  filter: drop-shadow(0 0 20px rgba(255, 215, 0, 0.4));
}

@keyframes splashLogoEntry {
  0%   { opacity: 0; transform: scale(0.5) rotate(-10deg); }
  100% { opacity: 1; transform: scale(1) rotate(0deg); }
}

@keyframes splashLogoFloat {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-8px); }
}

/* Title word-by-word reveal */
.splash-title {
  font-family: 'Poppins', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px;
  color: #FFFFFF;
}

.splash-word {
  display: inline-block;
  opacity: 0;
  animation: splashWordReveal 0.5s ease forwards;
  animation-delay: calc(0.4s + var(--i) * 0.2s);
  margin-right: 12px;
}

.splash-highlight {
  color: #FFD700;
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
}

@keyframes splashWordReveal {
  0%   { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

/* Tagline */
.splash-tagline {
  font-family: 'Inter', sans-serif;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
  animation-delay: 1s;
  margin: 0;
}

/* Loading bar */
.splash-loader {
  width: 200px;
  height: 3px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
  margin: 32px auto 0;
  overflow: hidden;
  opacity: 0;
  animation: fadeInUp 0.3s ease forwards;
  animation-delay: 1.2s;
}

.splash-loader-bar {
  height: 100%;
  width: 0%;
  background: linear-gradient(90deg, #FFD700, #FFC107, #FFD700);
  border-radius: 3px;
  animation: splashLoadBar 1.8s ease-in-out 1.3s forwards;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

@keyframes splashLoadBar {
  0%   { width: 0%; }
  30%  { width: 40%; }
  60%  { width: 70%; }
  90%  { width: 90%; }
  100% { width: 100%; }
}

/* Floating particles */
.splash-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.particle {
  position: absolute;
  left: var(--x);
  top: var(--y);
  width: var(--size);
  height: var(--size);
  background: rgba(255, 215, 0, 0.4);
  border-radius: 50%;
  animation: particleFloat 4s ease-in-out var(--delay) infinite;
}

@keyframes particleFloat {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.4; }
  50%      { transform: translateY(-30px) scale(1.5); opacity: 0.8; }
}

/* Responsive */
@media (max-width: 576px) {
  .splash-title { font-size: 1.8rem; }
  .splash-tagline { font-size: 0.85rem; }
  .splash-logo-img { width: 70px; }
  .splash-loader { width: 150px; }
}

@media (min-width: 992px) {
  .splash-title { font-size: 3.5rem; }
  .splash-tagline { font-size: 1.15rem; }
  .splash-logo-img { width: 120px; }
}
```

### Splash Screen JavaScript — Dismiss & Reveal

```javascript
document.addEventListener('DOMContentLoaded', function() {
  const splash = document.getElementById('splash-screen');
  if (!splash) return;

  // Minimum display time: 2.8 seconds (for animation to complete)
  const minDisplayTime = 2800;
  const startTime = Date.now();

  window.addEventListener('load', function() {
    const elapsed = Date.now() - startTime;
    const remaining = Math.max(0, minDisplayTime - elapsed);

    setTimeout(function() {
      splash.classList.add('fade-out');
      
      // Remove from DOM after fade
      setTimeout(function() {
        splash.remove();
        // Trigger hero animations after splash
        document.body.classList.add('page-revealed');
      }, 600);
    }, remaining);
  });

  // Fallback: force dismiss after 5 seconds even if page hasn't fully loaded
  setTimeout(function() {
    if (!splash.classList.contains('fade-out')) {
      splash.classList.add('fade-out');
      setTimeout(() => splash.remove(), 600);
    }
  }, 5000);
});
```

---

## Hero Section — Post-Splash Choreography

After the splash fades, the hero section animates in with a choreographed sequence:

```css
/* Hero elements start hidden */
.page-revealed .hero-logo      { animation: fadeInDown 0.6s ease 0.1s forwards; opacity: 0; }
.page-revealed .hero-title     { animation: fadeInUp 0.6s ease 0.2s forwards; opacity: 0; }
.page-revealed .hero-subtitle  { animation: fadeInUp 0.6s ease 0.4s forwards; opacity: 0; }
.page-revealed .hero-cta       { animation: scaleIn 0.5s ease 0.6s forwards; opacity: 0; }
.page-revealed .hero-cards     { animation: fadeInUp 0.6s ease 0.8s forwards; opacity: 0; }
.page-revealed .navbar         { animation: fadeInDown 0.4s ease 0.0s forwards; opacity: 0; }
```

---

## Mini Preloader (For AJAX / Page Navigation)

Use this lighter preloader for in-page transitions (e.g., when loading search results):

```css
.mini-loader {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  z-index: 10000;
  pointer-events: none;
}

.mini-loader-bar {
  height: 100%;
  width: 0%;
  background: linear-gradient(90deg, #FFD700, #4A2C8A, #FFD700);
  background-size: 200% 100%;
  animation: miniLoaderProgress 2s ease-in-out infinite, shimmer 1.5s linear infinite;
  border-radius: 0 2px 2px 0;
}

@keyframes miniLoaderProgress {
  0%   { width: 0%; }
  50%  { width: 70%; }
  100% { width: 100%; }
}
```

```html
<!-- Place at top of body -->
<div id="mini-loader" class="mini-loader" style="display:none;">
  <div class="mini-loader-bar"></div>
</div>
```

```javascript
// Show/hide for navigation
function showLoader() {
  document.getElementById('mini-loader').style.display = 'block';
}
function hideLoader() {
  const loader = document.getElementById('mini-loader');
  loader.style.opacity = '0';
  setTimeout(() => { loader.style.display = 'none'; loader.style.opacity = '1'; }, 300);
}
```

---

## Competition Tips — First Impression Checklist

1. **Splash → Hero** transition must feel seamless (same purple gradient)
2. **Load time < 3s** perceived — the splash masks any actual loading
3. **Logo should be crisp** — use SVG or high-res PNG (min 2x)
4. **Gold accents** on the splash create premium feel
5. **Particles** add depth without cluttering
6. **Sound? No.** Auto-playing audio annoys judges
7. **Fallback**: If JS fails, the splash auto-hides via CSS fallback:

```css
/* Fallback: hide splash after animation regardless of JS */
.splash-screen {
  animation: splashFallbackHide 0s 5s forwards;
}
@keyframes splashFallbackHide {
  to { opacity: 0; visibility: hidden; pointer-events: none; }
}
```

## Session-Based Splash (Show Only Once)

To avoid annoying returning users, only show splash on first visit per session:

```javascript
document.addEventListener('DOMContentLoaded', function() {
  const splash = document.getElementById('splash-screen');
  if (!splash) return;

  // Skip splash if already seen this session
  if (sessionStorage.getItem('cmp-splash-seen')) {
    splash.remove();
    document.body.classList.add('page-revealed');
    return;
  }

  sessionStorage.setItem('cmp-splash-seen', 'true');
  // ... rest of splash logic
});
```
