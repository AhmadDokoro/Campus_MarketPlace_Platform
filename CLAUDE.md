# Campus Marketplace Platform — CLAUDE.md

## Project overview
A Spring Boot + Thymeleaf campus marketplace for UMT students. Buyers browse and purchase products; sellers list items; admins manage the platform. Server-side rendered (no React/Vue).

**Stack:** Java 17, Spring Boot 3.5.4, Thymeleaf, JPA/Hibernate, MySQL, Bootstrap 4.6, Cloudinary (image hosting), SMTP email (Gmail).

**Run the app:**
```
.\mvnw.cmd package -DskipTests   # build
java -jar target\campusmarket-0.0.1-SNAPSHOT.jar   # run
```
Runs on `http://localhost:8080`. Default admin/test login: `ahmadkabirudokoro@gmail.com` / `12345`.

**Database:** MySQL at `localhost:3306/campus_marketplace` (root / Ahmadakd003). DDL mode is `update` during development.

---

## Key directories

| Path | Purpose |
|---|---|
| `src/main/java/.../controller/` | Spring MVC controllers (one per domain) |
| `src/main/java/.../model/` | JPA entities |
| `src/main/java/.../service/` | Business logic |
| `src/main/java/.../repositories/` | Spring Data JPA repos |
| `src/main/resources/templates/` | Thymeleaf HTML pages |
| `src/main/resources/templates/fragments/` | Shared header, footer, product-card |
| `src/main/resources/static/css/` | CSS files (see below) |
| `src/main/resources/static/js/` | JS files |
| `src/main/resources/static/lib/` | AOS animation library |

---

## CSS files — what each does

| File | Scope |
|---|---|
| `style.css` | Global base styles |
| `mobile.css` | **All mobile overrides** — bottom nav, search dropdown, responsive grid. Touch this for any mobile layout fix. |
| `cmp-enhance.css` | AOS animation enhancements; includes `html:not(.aos-init) [data-aos]` visibility guard |
| `cmp-stunning.css` | Visual polish / luxury theme effects |
| `seller-shell.css` | Seller dashboard styles |
| `admin.css` | Admin dashboard styles |
| `authentication.css` | Login / signup pages |

---

## Mobile layout rules

- **Bottom nav** (`mob-bottom-nav`): `position: fixed; z-index: 999999; bottom: 0`. Uses `html body .mob-bottom-nav` high-specificity selector. Present in: `index.html`, `product-list.html`, `cart.html`, `profile.html`. Active item gets `.mob-nav-active`.
- **Product grids on mobile**: `grid-template-columns: repeat(2, minmax(0,1fr)) !important` — enforced in `mobile.css` section 32. Never use a `1fr` override below it or it will revert to 1 column.
- **AOS animations**: Use `data-aos="fade-up"` on grid items, NOT `zoom-in`. `zoom-in` applies `scale(0.6)` during init which causes a visual snap/flash.
- **Profile page mobile** (`@media max-width: 767.98px`): Dark luxury theme — deep purple/black background, gold accents. Order tracker uses `flex:1` per step (not fixed `100px`) so all 5 status steps fit in one row without horizontal scroll.
- **Mobile search dropdown** (`#mobile-search-results`): `position: fixed; left: 10px; right: 10px; width: auto` — spans near-full viewport width. Background `#0f0625`, gold price text, white title text.
- **Safe padding for bottom nav**: Pages need `padding-bottom: 90px` on `.page-wrap` at mobile to avoid content hiding behind the nav bar.

---

## Thymeleaf patterns

- Fragments included via `th:replace="fragments/header :: header"`.
- Session user check: `th:if="${session.userId != null}"`.
- Product card fragment: `th:replace="fragments/product-card :: card(${p})"`.
- Category filter URLs: `@{'/products/category/' + ${cat.categoryId}}`.

---

## CSS specificity notes

- `!important` rules resolve by **source order** — a later `!important` beats an earlier one at the same specificity. When a mobile fix isn't sticking, check whether a rule lower in the same file (or in a page `<style>` block that loads after `mobile.css`) is overriding it.
- The page inline `<style>` blocks in individual templates load **after** `mobile.css`, so inline `!important` beats `mobile.css !important` when specificity is equal.

---

## Git workflow

- Active dev branch: `dev`
- Production branch: `master`
- After completing a feature/fix: commit on `dev` → merge to `master` → push `origin master` → switch back to `dev`.
- Remote: `https://github.com/AhmadDokoro/Campus_MarketPlace_Platform.git`

---

## External services

| Service | Used for |
|---|---|
| Cloudinary (`dg6e4oagz`) | Product image upload and hosting |
| Gmail SMTP (`campus.marketplace.umt@gmail.com`) | Order/notification emails |
| Pexels API | Server-side image library (not browser-exposed) |

---

## Known gotchas

- **Don't add `@media (max-width: 360px) { grid-template-columns: 1fr }` overrides** — this was the root cause of browse grid reverting to 1 column on small phones.
- `spring.thymeleaf.cache=false` is set — template changes reflect on reload without rebuild during dev, but CSS/JS static files still require a rebuild (`mvnw package`) to be served from the JAR.
- The `mob-bottom-nav` uses `d-lg-none` (Bootstrap hidden at ≥992px) — this doesn't set `display` at mobile, so `mobile.css` must explicitly set it to `flex` inside the `@media (max-width: 991px)` block.
