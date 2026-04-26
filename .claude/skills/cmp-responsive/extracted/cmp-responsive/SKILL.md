---
name: cmp-responsive
description: >
  Campus Marketplace Platform mobile responsiveness and adaptive layout skill for Thymeleaf templates.
  Use this skill whenever Kabiru asks about mobile layout, responsive design, screen sizes, 
  breakpoints, hamburger menu, mobile navigation, touch-friendly UI, viewport, media queries, 
  tablet view, phone view, "looks bad on mobile", "fix on small screen", grid layout, flexbox, 
  or any layout/sizing concern on the Campus Marketplace Platform. Also trigger when creating 
  any new page or component to ensure it is mobile-first from the start. Every page built for 
  this project MUST follow these responsive patterns.
---

# Campus Marketplace Platform — Mobile Responsiveness

## Overview
This skill ensures every page of the Campus Marketplace Platform looks flawless on 
all screen sizes — from 320px phones to 4K monitors. The approach is **mobile-first**: 
write styles for mobile, then add complexity for larger screens.

Think of it like building a house: you lay the small foundation first (mobile), then 
expand rooms as you have more space (tablet → desktop).

## Viewport Meta — REQUIRED in layout `<head>`
```html
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
```

## Breakpoint System

```css
/* ===== BREAKPOINTS (Mobile-First) ===== */
/* Base styles = Mobile (0 - 575px) — write these FIRST */

/* Small devices (landscape phones) */
@media (min-width: 576px)  { /* sm */ }

/* Medium devices (tablets) */
@media (min-width: 768px)  { /* md */ }

/* Large devices (desktops) */
@media (min-width: 992px)  { /* lg */ }

/* Extra large (wide screens) */
@media (min-width: 1200px) { /* xl */ }

/* Ultra wide */
@media (min-width: 1400px) { /* xxl */ }
```

### CSS Variables for Breakpoints
```css
:root {
  --container-sm:  540px;
  --container-md:  720px;
  --container-lg:  960px;
  --container-xl:  1140px;
  --container-xxl: 1320px;
}
```

## Layout Patterns

### 1. Responsive Container
```css
.cmp-container {
  width: 100%;
  padding-left: 16px;
  padding-right: 16px;
  margin: 0 auto;
}

@media (min-width: 576px)  { .cmp-container { max-width: var(--container-sm); } }
@media (min-width: 768px)  { .cmp-container { max-width: var(--container-md); padding: 0 24px; } }
@media (min-width: 992px)  { .cmp-container { max-width: var(--container-lg); } }
@media (min-width: 1200px) { .cmp-container { max-width: var(--container-xl); } }
@media (min-width: 1400px) { .cmp-container { max-width: var(--container-xxl); } }
```

### 2. Product Grid (Auto-Responsive)
```css
/* This one grid rule handles ALL screen sizes automatically */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  padding: 16px;
}

/* On very small phones, force single column */
@media (max-width: 575px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 12px;
  }
}
```

### 3. Two-Column to Stack
```css
.cmp-split {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (min-width: 768px) {
  .cmp-split {
    flex-direction: row;
    gap: 32px;
  }
  .cmp-split-main    { flex: 2; }
  .cmp-split-sidebar { flex: 1; }
}
```

### 4. Hero Section Responsive
```css
.cmp-hero {
  padding: 40px 16px;
  text-align: center;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.cmp-hero h1 {
  font-size: 1.8rem;
  line-height: 1.2;
}
.cmp-hero p {
  font-size: 1rem;
  max-width: 90%;
}

@media (min-width: 768px) {
  .cmp-hero {
    padding: 60px 32px;
    min-height: 450px;
  }
  .cmp-hero h1 { font-size: 2.5rem; }
  .cmp-hero p  { font-size: 1.15rem; max-width: 600px; }
}

@media (min-width: 992px) {
  .cmp-hero {
    padding: 80px 48px;
    min-height: 550px;
  }
  .cmp-hero h1 { font-size: 3.2rem; }
  .cmp-hero p  { font-size: 1.25rem; max-width: 700px; }
}
```

## Component Responsive Rules

### Navigation — Mobile Hamburger
```css
/* Mobile nav (default) */
.cmp-nav-toggle {
  display: flex;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
  padding: 8px;
  z-index: 1001;
  background: none;
  border: none;
}
.cmp-nav-toggle span {
  width: 28px;
  height: 3px;
  background: var(--cmp-white);
  border-radius: 3px;
  transition: all 0.3s ease;
}
/* Hamburger → X animation */
.cmp-nav-toggle.active span:nth-child(1) { transform: rotate(45deg) translate(6px, 6px); }
.cmp-nav-toggle.active span:nth-child(2) { opacity: 0; }
.cmp-nav-toggle.active span:nth-child(3) { transform: rotate(-45deg) translate(6px, -6px); }

.cmp-nav-links {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--cmp-purple-deep);
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  z-index: 1000;
}
.cmp-nav-links.active {
  display: flex;
}
.cmp-nav-links a {
  font-size: 1.4rem;
  color: var(--cmp-white);
  padding: 12px 24px;
}

/* Desktop nav */
@media (min-width: 992px) {
  .cmp-nav-toggle { display: none; }
  .cmp-nav-links {
    display: flex !important;
    position: static;
    flex-direction: row;
    background: transparent;
    gap: 8px;
  }
  .cmp-nav-links a {
    font-size: 0.95rem;
    padding: 8px 16px;
  }
}
```

### Cards — Responsive Sizing
```css
.cmp-card {
  width: 100%;
}
.cmp-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

@media (min-width: 768px) {
  .cmp-card img { height: 220px; }
}

@media (min-width: 1200px) {
  .cmp-card img { height: 250px; }
}
```

### Category Bar — Horizontal Scroll on Mobile
```css
.category-bar {
  display: flex;
  overflow-x: auto;
  gap: 8px;
  padding: 12px 16px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.category-bar::-webkit-scrollbar { display: none; }
.category-chip {
  flex-shrink: 0;
  padding: 8px 18px;
  border-radius: var(--radius-full);
  white-space: nowrap;
  font-size: 0.85rem;
  background: var(--cmp-purple-soft);
  color: var(--cmp-purple-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}
.category-chip:hover,
.category-chip.active {
  background: var(--cmp-purple-primary);
  color: var(--cmp-white);
}

@media (min-width: 768px) {
  .category-bar {
    flex-wrap: wrap;
    overflow-x: visible;
    justify-content: center;
  }
}
```

### Search Bar — Responsive
```css
.cmp-search {
  width: 100%;
  max-width: 100%;
  padding: 10px 16px;
}

@media (min-width: 768px) {
  .cmp-search { max-width: 500px; }
}

@media (min-width: 1200px) {
  .cmp-search { max-width: 600px; }
}
```

### Footer — Stack on Mobile
```css
.cmp-footer-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  padding: 32px 16px;
}

@media (min-width: 768px) {
  .cmp-footer-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (min-width: 992px) {
  .cmp-footer-grid { grid-template-columns: repeat(4, 1fr); padding: 48px 32px; }
}
```

### Tables — Horizontal Scroll on Mobile
```css
.table-responsive {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.table-responsive table {
  min-width: 600px;
}
```

## Typography Scale (Responsive)
```css
/* Mobile-first typography */
h1 { font-size: 1.75rem; line-height: 1.2; }
h2 { font-size: 1.5rem;  line-height: 1.25; }
h3 { font-size: 1.25rem; line-height: 1.3; }
h4 { font-size: 1.1rem;  line-height: 1.35; }
p  { font-size: 0.95rem; line-height: 1.6; }

@media (min-width: 768px) {
  h1 { font-size: 2.25rem; }
  h2 { font-size: 1.75rem; }
  h3 { font-size: 1.4rem;  }
  p  { font-size: 1rem;    }
}

@media (min-width: 1200px) {
  h1 { font-size: 3rem;    }
  h2 { font-size: 2rem;    }
  h3 { font-size: 1.5rem;  }
  p  { font-size: 1.05rem; }
}
```

## Touch-Friendly Rules

```css
/* Minimum touch target size — 44x44px (Apple HIG / WCAG) */
.cmp-touchable {
  min-height: 44px;
  min-width: 44px;
  padding: 12px;
}

/* Buttons on mobile */
@media (max-width: 767px) {
  .btn-cmp-primary,
  .btn-cmp-secondary {
    width: 100%;
    padding: 14px 24px;
    font-size: 1rem;
  }
}

/* Disable hover effects on touch */
@media (hover: none) {
  .cmp-card-animated:hover {
    transform: none;
    box-shadow: var(--shadow-md);
  }
}
```

## Image Optimization
```css
/* Responsive images */
img {
  max-width: 100%;
  height: auto;
  display: block;
}

/* Aspect ratio container for product images */
.img-aspect {
  position: relative;
  width: 100%;
  padding-bottom: 75%; /* 4:3 ratio */
  overflow: hidden;
}
.img-aspect img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
```

## Testing Checklist
Before any page is done, verify at these widths:
1. **320px** — Small phones (iPhone SE)
2. **375px** — Standard phones (iPhone 12/13)
3. **428px** — Large phones (iPhone 14 Pro Max)
4. **768px** — Tablets (iPad)
5. **1024px** — Small laptops / iPad Pro
6. **1440px** — Standard desktop
7. **1920px** — Full HD monitors
