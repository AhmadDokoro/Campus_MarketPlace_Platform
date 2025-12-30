# Campus Marketplace Platform

The Campus Marketplace Platform is a web-based system designed to facilitate secure
buying and selling of products among students within Universiti Malaysia Terengganu (UMT).
The platform supports student-to-student trading only and operates strictly within the
campus environment.

This project follows a structured, layered architecture and is implemented using
Spring Boot, Thymeleaf, and MySQL.

---

## 🎯 System Purpose

The system aims to:
- Provide a centralized marketplace for UMT students
- Enable secure transactions between buyers and sellers
- Ensure seller authenticity through administrative verification
- Support structured communication between buyers and sellers after purchase
- Maintain clean separation of concerns in architecture and database design

---

## 🧑‍💼 User Roles

### Buyer
- Browse and search products
- Add products to cart
- Place orders and make payments
- Track order and delivery status
- Chat with sellers after payment
- Review sellers after completed orders

### Seller
- Register and request seller verification
- List, edit, and delete products (only if approved)
- Upload product images
- View sales history
- Manage delivery status for orders
- Communicate with buyers via system chat

### Administrator (Admin)
- Review and verify seller accounts
- Approve or reject seller verification requests
- Monitor system activities
- Review AI-flagged products
- Manage product categories
- View system statistics

---

## 🧩 Core System Modules

1. **Manage Seller Verification**
   - Seller submits verification request
   - Admin reviews and approves or rejects sellers

2. **Manage System Activities**
   - View system statistics
   - Review AI-flagged products

3. **Explore Products**
   - Browse products
   - View product details
   - Search and filter products
   - Add products to cart

4. **Manage Orders**
   - Purchase products
   - Make payments
   - Track delivery status
   - System chat between buyer and seller
   - Review seller after order completion

5. **Manage Product Listings**
   - List new products
   - Edit existing products
   - Delete products
   - Manage product categories (Admin)

6. **Manage Sales**
   - View sales history
   - Receive payment notifications
   - Update delivery status

> Authentication and authorization are implemented using role-based access control.

---

## 🏗️ System Architecture

The system follows a **three-tier layered architecture**:

- **Presentation Layer**
  - Thymeleaf templates
  - Handles user interactions and UI rendering

- **Business Logic Layer**
  - Spring Boot services
  - Implements business rules such as seller verification, order processing, and chat control

- **Data Access Layer**
  - Spring Data JPA (Hibernate)
  - Handles database persistence and retrieval

External services:
- Payment Gateway (ToyyibPay)
- Image Storage (Cloudinary)
- AI Service for product moderation

---

## 🧠 Critical Business Rules (IMPORTANT)

The following rules must always be enforced in implementation:

- Only **APPROVED sellers** can list products
- Each user has **exactly one cart**
- Each order has **exactly one payment**
- Each order has **exactly one chat**
- Chat is created **only after successful payment**
- Product images are limited to **maximum of 2 images per product**
- Product moderation uses AI with statuses:
  - `UNKNOWN`, `SUSPICIOUS`, `VERIFIED`
- Reviews can only be submitted **after order completion**
- Orders are campus-only (no off-campus delivery)

---

## 🗄️ Database Design (Source of Truth)

The database is implemented in **MySQL** and normalized up to **Third Normal Form (3NF)**.

### Core Tables
- `users`
- `sellers`
- `user_addresses`
- `categories`
- `products`
- `product_images`
- `cart`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `chats`
- `messages`
- `reviews`

### Key Relationships
- `users` ↔ `sellers` (1:1)
- `sellers` ↔ `products` (1:N)
- `products` ↔ `product_images` (1:N)
- `users` ↔ `cart` (1:1)
- `orders` ↔ `payments` (1:1)
- `orders` ↔ `chats` (1:1)
- `chats` ↔ `messages` (1:N)
- `orders` ↔ `order_items` (1:N)

> JPA entities must strictly follow the finalized database schema.

---

## 🛠️ Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA (Hibernate)
- Spring Security
- Thymeleaf
- MySQL
- Cloudinary (Image Storage)
- Paystack (Payment Gateway)
- AI Service (Product Moderation)

---

## 🚧 Development Guidelines

- Controllers must be thin (no business logic)
- Services enforce business rules
- Repositories handle data access only
- Database schema is authoritative
- Changes to schema must be reflected across all layers
- Modules should be implemented incrementally

---

## 📄 Academic Documents

The following documents define the formal design and requirements:
- Software Requirements Specification (SRS)
- Software Design Document (SDD)
- Software Project Management Plan (SPMP)

These documents are included for academic reference and validation.

---

## ✅ Implementation Strategy

Development should proceed **module-by-module**, following:
1. Database structure
2. Business rules
3. Service logic
4. Controller endpoints
5. UI integration

This ensures consistency, correctness, and maintainability.


