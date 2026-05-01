---
name: cmp-ui-contrast
description: >
  Campus Marketplace Platform UI contrast, color palette, and design system skill for Thymeleaf templates.
  Use this skill whenever Kabiru asks to style, theme, color, fix contrast, improve UI appearance, 
  apply branding, fix readability, design any page, create a new page, or touch any visual aspect of 
  the Campus Marketplace Platform. Also trigger when asked about "making it look good", "professional UI", 
  "color scheme", "dark mode", "light mode", "branding", "UMT colors", or any CSS styling task for the 
  Campus Marketplace project. This skill defines the SINGLE SOURCE OF TRUTH for all colors, spacing, 
  typography, shadows, and visual hierarchy in the project.
---

# Campus Marketplace Platform — UI Contrast & Design System

## Overview
This skill defines the complete visual design system for the UMT Campus Marketplace Platform.
Every Thymeleaf template, CSS file, or component MUST follow these rules to maintain visual consistency
and competition-winning aesthetics.

## Color Palette (extracted from the live site)

### CSS Variables — Always inject these into the root stylesheet

```css
:root {
  /* ===== PRIMARY PALETTE ===== */
  --cmp-purple-deep:    #1A0A3E;   /* Deepest purple — top nav bar, footer */
  --cmp-purple-primary: #2D1B69;   /* Primary purple — main header, hero overlays */
  --cmp-purple-mid:     #4A2C8A;   /* Mid purple — hover states, active tabs */
  --cmp-purple-light:   #6B4EAF;   /* Light purple — secondary buttons, tags */
  --cmp-purple-soft:    #E8E0F5;   /* Soft purple — card backgrounds, subtle tints */

  /* ===== ACCENT / GOLD ===== */
  --cmp-gold-primary:   #FFD700;   /* Primary gold — CTA buttons, highlights, badges */
  --cmp-gold-hover:     #FFC107;   /* Gold hover state */
  --cmp-gold-soft:      #FFF8E1;   /* Soft gold — notification backgrounds */
  --cmp-gold-dark:      #C9A800;   /* Dark gold — text on light backgrounds */

  /* ===== NEUTRALS ===== */
  --cmp-white:          #FFFFFF;
  --cmp-off-white:      #F8F6FC;   /* Page background — slight purple tint */
  --cmp-gray-100:       #F1EEF6;   /* Card backgrounds */
  --cmp-gray-200:       #E0DBE8;   /* Borders, dividers */
  --cmp-gray-300:       #B8B0C4;   /* Placeholder text */
  --cmp-gray-600:       #6B6280;   /* Secondary text */
  --cmp-gray-800:       #2E2740;   /* Body text */
  --cmp-gray-900:       #1A1528;   /* Headings */

  /* ===== SEMANTIC ===== */
  --cmp-success:        #22C55E;   /* Verified, success */
  --cmp-success-bg:     #F0FDF4;
  --cmp-warning:        #F59E0B;   /* Pending, caution */
  --cmp-warning-bg:     #FFFBEB;
  --cmp-danger:         #EF4444;   /* Error, delete, flagged */
  --cmp-danger-bg:      #FEF2F2;
  --cmp-info:           #3B82F6;   /* Info, links */
  --cmp-info-bg:        #EFF6FF;

  /* ===== SPACING SCALE (8px base) ===== */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  16px;
  --space-lg:  24px;
  --space-xl:  32px;
  --space-2xl: 48px;
  --space-3xl: 64px;

  /* ===== TYPOGRAPHY ===== */
  --font-primary:   'Poppins', 'Segoe UI', sans-serif;
  --font-secondary: 'Inter', 'Segoe UI', sans-serif;
  --font-mono:      'JetBrains Mono', 'Fira Code', monospace;

  /* ===== SHADOWS ===== */
  --shadow-sm:    0 1px 2px rgba(26, 10, 62, 0.06);
  --shadow-md:    0 4px 12px rgba(26, 10, 62, 0.08);
  --shadow-lg:    0 8px 24px rgba(26, 10, 62, 0.12);
  --shadow-xl:    0 16px 48px rgba(26, 10, 62, 0.16);
  --shadow-gold:  0 4px 16px rgba(255, 215, 0, 0.3);

  /* ===== BORDERS ===== */
  --radius-sm:  6px;
  --radius-md:  10px;
  --radius-lg:  16px;
  --radius-xl:  24px;
  --radius-full: 9999px;

  /* ===== TRANSITIONS ===== */
  --ease-default: cubic-bezier(0.4, 0, 0.2, 1);
  --duration-fast: 150ms;
  --duration-normal: 300ms;
  --duration-slow: 500ms;
}
```

### Google Fonts — Add to `<head>` of layout template
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
```

## Contrast Rules (WCAG AA Compliance)

| Background              | Text Color           | Min Ratio | Use Case                    |
|--------------------------|----------------------|-----------|------------------------------|
| `--cmp-purple-deep`     | `--cmp-white`        | 12.5:1    | Nav text, footer text        |
| `--cmp-purple-primary`  | `--cmp-white`        | 10.2:1    | Hero headings, buttons       |
| `--cmp-purple-primary`  | `--cmp-gold-primary` | 6.8:1     | Gold accents on purple       |
| `--cmp-white`           | `--cmp-gray-900`     | 16:1      | Body text on white cards     |
| `--cmp-white`           | `--cmp-purple-primary`| 10.2:1   | Purple headings on white     |
| `--cmp-gold-primary`    | `--cmp-purple-deep`  | 8.5:1     | Purple text on gold buttons  |
| `--cmp-off-white`       | `--cmp-gray-800`     | 12.8:1    | Body text on page background |

### Rules
1. **NEVER** use light text on light backgrounds or dark text on dark backgrounds.
2. **Gold text** (`--cmp-gold-primary`) is ONLY used on `--cmp-purple-deep` or `--cmp-purple-primary` backgrounds.
3. **White text** is ONLY used on purple or dark backgrounds.
4. **Purple text** on white/light backgrounds must use `--cmp-purple-primary` or darker.
5. **Body text** always uses `--cmp-gray-800` on light backgrounds.
6. **Links** use `--cmp-purple-mid` with underline on hover.

## Component Patterns

### Buttons
```css
/* Primary CTA — Gold */
.btn-cmp-primary {
  background: linear-gradient(135deg, var(--cmp-gold-primary), var(--cmp-gold-hover));
  color: var(--cmp-purple-deep);
  font-weight: 600;
  padding: 12px 28px;
  border: none;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-gold);
  transition: all var(--duration-normal) var(--ease-default);
  cursor: pointer;
}
.btn-cmp-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 215, 0, 0.45);
}

/* Secondary — Purple */
.btn-cmp-secondary {
  background: var(--cmp-purple-primary);
  color: var(--cmp-white);
  font-weight: 500;
  padding: 10px 24px;
  border: none;
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--ease-default);
}
.btn-cmp-secondary:hover {
  background: var(--cmp-purple-mid);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* Outline */
.btn-cmp-outline {
  background: transparent;
  color: var(--cmp-purple-primary);
  border: 2px solid var(--cmp-purple-primary);
  padding: 10px 24px;
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--ease-default);
}
.btn-cmp-outline:hover {
  background: var(--cmp-purple-primary);
  color: var(--cmp-white);
}
```

### Cards
```css
.cmp-card {
  background: var(--cmp-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  transition: all var(--duration-normal) var(--ease-default);
  border: 1px solid var(--cmp-gray-200);
}
.cmp-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
  border-color: var(--cmp-purple-light);
}
.cmp-card-header {
  background: linear-gradient(135deg, var(--cmp-purple-deep), var(--cmp-purple-primary));
  padding: var(--space-lg);
  color: var(--cmp-white);
}
.cmp-card-body {
  padding: var(--space-lg);
}
```

### Navigation Bar
```css
.cmp-navbar {
  background: linear-gradient(135deg, var(--cmp-purple-deep), var(--cmp-purple-primary));
  padding: 0 var(--space-xl);
  box-shadow: var(--shadow-lg);
  position: sticky;
  top: 0;
  z-index: 1000;
}
.cmp-navbar a {
  color: var(--cmp-white);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--duration-fast) var(--ease-default);
}
.cmp-navbar a:hover,
.cmp-navbar a.active {
  color: var(--cmp-gold-primary);
}
```

### Form Inputs
```css
.cmp-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--cmp-gray-200);
  border-radius: var(--radius-md);
  font-family: var(--font-secondary);
  font-size: 0.95rem;
  color: var(--cmp-gray-800);
  background: var(--cmp-white);
  transition: all var(--duration-fast) var(--ease-default);
}
.cmp-input:focus {
  outline: none;
  border-color: var(--cmp-purple-mid);
  box-shadow: 0 0 0 3px rgba(75, 44, 138, 0.15);
}
.cmp-input::placeholder {
  color: var(--cmp-gray-300);
}
```

### Badges & Status
```css
.cmp-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 0.8rem;
  font-weight: 600;
}
.cmp-badge-verified { background: var(--cmp-success-bg); color: #166534; }
.cmp-badge-pending  { background: var(--cmp-warning-bg); color: #92400E; }
.cmp-badge-flagged  { background: var(--cmp-danger-bg);  color: #991B1B; }
.cmp-badge-gold     { background: var(--cmp-gold-soft);  color: var(--cmp-gold-dark); }
```

## Page Background Pattern
Always use this soft gradient for the page body:
```css
body {
  background: var(--cmp-off-white);
  font-family: var(--font-secondary);
  color: var(--cmp-gray-800);
  min-height: 100vh;
}
```

## When Styling Any Page
1. Always import the CSS variables first
2. Use ONLY the defined palette — never hardcode hex values
3. Test every text/background combo against the contrast table
4. Apply consistent spacing using the spacing scale
5. Use the shadow scale for depth hierarchy
6. Headings use `font-family: var(--font-primary)` (Poppins)
7. Body text uses `font-family: var(--font-secondary)` (Inter)
