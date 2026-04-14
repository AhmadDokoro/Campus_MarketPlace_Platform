# 🛒 Campus Marketplace Platform

> A secure student-to-student marketplace for Universiti Malaysia Terengganu (UMT) — built with Spring Boot, Thymeleaf, and MySQL.

---

## 📋 Overview

The Campus Marketplace Platform (CMP) is a full-stack web application that enables UMT students to buy and sell products within the campus environment. The platform enforces seller identity verification, AI-powered listing moderation, and structured order tracking — all within a clean layered architecture.

---

## ✨ Features

### 🧑‍💼 For Buyers
- Browse and search products by title or category
- Add products to cart and place orders
- Pay online (mock gateway) or arrange campus meetup
- Track delivery status per order item
- Chat directly with sellers after payment
- Leave reviews for sellers after completed orders

### 🏪 For Sellers
- Request seller verification with student ID documents
- List, edit, and delete products (approved sellers only)
- Upload product images or search from a built-in image library (Pexels)
- View sales history with total revenue summary
- Update delivery status for each order
- Communicate with buyers via in-platform chat

### 🛡️ For Admins
- Review and approve/reject seller verification requests
- View live dashboard analytics (users, listings, revenue, charts)
- Review AI-flagged suspicious product listings
- Manage product categories and academic mentors

---

## 🤖 AI Fraud Detection

Every new product listing is automatically analysed by OpenAI (`gpt-4o-mini`) for signs of fraud or misleading pricing. Products are classified as:

| Status | Meaning |
|---|---|
| `VERIFIED` | Appears genuine and reasonably priced |
| `SUSPICIOUS` | Flagged for admin review |
| `UNKNOWN` | API unavailable at time of listing (fallback) |

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 + Spring Boot 3.5.4 |
| Templating | Thymeleaf |
| Database | MySQL |
| ORM | Spring Data JPA (Hibernate 6) |
| Image Storage | Cloudinary |
| AI Moderation | OpenAI API (`gpt-4o-mini`) |
| Image Library | Pexels API (server-side proxy) |
| Payment | Mock ToyyibPay gateway |
| Build Tool | Maven |

---

## ⚙️ Setup & Configuration

### Prerequisites
- Java 17+
- MySQL 8+
- Maven (or use included `mvnw`)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/campusmarket.git
cd campusmarket
```

### 2. Create the database
```sql
CREATE DATABASE campus_marketplace;
```

### 3. Configure `application.properties`

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/campus_marketplace
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Cloudinary
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.api_secret=YOUR_API_SECRET

# OpenAI (fraud detection)
OPENAI_API_KEY=YOUR_OPENAI_KEY

# Pexels (image library)
pexels.api.key=YOUR_PEXELS_KEY

# Email (Gmail SMTP)
spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

The app starts on **`http://localhost:8080`**.

---

## 🗄️ Database Schema

The database is normalized to **3NF** with the following core tables:

```
users → sellers → products → product_images
users → cart → cart_items → products
users → orders → order_items → products / sellers
orders → payments
order_items → chats → messages
orders → reviews
```

Schema is auto-managed by Hibernate (`ddl-auto=update`). Switch to `validate` for production.

---

## 👥 User Roles

| Role | Access |
|---|---|
| `BUYER` | Browse, cart, checkout, orders, chat, reviews |
| `SELLER` | All buyer access + product listings, sales, delivery management |
| `ADMIN` | All seller access + verification approval, flagged products, categories |

Authentication is **session-based** (no Spring Security token). Role is stored in `HttpSession` on login.

---

## 💳 Mock Payment Cards

For testing the payment flow:

**Cards that succeed:**
- `4242 4242 4242 4242`
- `4111 1111 1111 1111`
- `5500 0000 0000 0004`

**Cards that fail:**
- `4000 0000 0000 0002` — Declined
- `4000 0000 0000 0069` — Expired
- `4000 0000 0000 9995` — Insufficient funds

---

## 📁 Project Structure

```
src/main/java/com/ahsmart/campusmarket/
├── controller/       — HTTP layer (thin, no business logic)
├── service/          — Business rules and external API calls
├── repositories/     — Spring Data JPA interfaces
├── model/            — JPA entities and enums
├── helper/           — Spring beans for Thymeleaf + configs
├── payloadDTOs/      — DTOs for AJAX/JSON responses
├── config/           — App-level Spring configuration
└── exceptions/       — Custom exception classes

src/main/resources/
├── templates/        — Thymeleaf HTML templates
├── static/           — CSS, JS, images
└── application.properties
```

---

## 📄 License

This project is developed for academic purposes at Universiti Malaysia Terengganu (UMT).

---

## 👨‍💻 Author

Developed by Ahmad — UMT Software Engineering student.
