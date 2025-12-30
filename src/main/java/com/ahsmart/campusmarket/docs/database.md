# Database Design – Campus Marketplace Platform

This document defines the FINAL database schema for the Campus Marketplace Platform.
All JPA entities must strictly follow this design.

Database Engine: MySQL  
Normalization Level: Third Normal Form (3NF)

---

## users

Stores all system users (Buyer, Seller, Admin).

| Column | Type | Constraints |
|------|------|------------|
| user_id | BIGINT | PK, Auto Increment |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| user_password | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | Nullable |
| academic_id | VARCHAR(50) | UNIQUE, NOT NULL |
| level | VARCHAR(50) | Nullable |
| mentor_name | VARCHAR(100) | Nullable |
| mentor_email | VARCHAR(255) | Nullable |
| role | ENUM | BUYER, SELLER, ADMIN |
| created_at | TIMESTAMP | Default CURRENT_TIMESTAMP |

---

## sellers

Stores seller verification data.

| Column | Type | Constraints |
|------|------|------------|
| seller_id | BIGINT | PK |
| user_id | BIGINT | FK → users.user_id, UNIQUE |
| id_card_image_url | VARCHAR(500) | NOT NULL |
| mynemo_profile_url | VARCHAR(500) | Nullable |
| status | ENUM | PENDING, APPROVED, REJECTED |
| reviewer_id | BIGINT | FK → users.user_id |
| submitted_at | TIMESTAMP | Default CURRENT_TIMESTAMP |

---

## user_addresses

Stores campus and optional off-campus addresses.

| Column | Type |
|------|------|
| address_id | BIGINT (PK) |
| user_id | BIGINT (FK) |
| hostel_block | VARCHAR(50) |
| floor | VARCHAR(20) |
| room_number | VARCHAR(20) |
| city | VARCHAR(100) |
| state | VARCHAR(100) |

---

## categories

Product categories.

| Column | Type |
|------|------|
| category_id | BIGINT (PK) |
| category_name | VARCHAR(100), UNIQUE |
| description | VARCHAR(255) |

---

## products

Stores product listings.

| Column | Type |
|------|------|
| product_id | BIGINT (PK) |
| seller_id | BIGINT (FK → sellers) |
| category_id | BIGINT (FK → categories) |
| title | VARCHAR(255) |
| description | TEXT |
| price | DECIMAL(10,2) |
| quantity | INT |
| condition | ENUM (NEW, USED) |
| flagged_status | ENUM (UNKNOWN, SUSPICIOUS, VERIFIED) |
| created_at | TIMESTAMP |

---

## product_images

Stores product images.

| Column | Type |
|------|------|
| image_id | BIGINT (PK) |
| product_id | BIGINT (FK) |
| image_url | VARCHAR(500) |
| public_id | VARCHAR(255) |
| is_primary | BOOLEAN |
| uploaded_at | TIMESTAMP |

Max 2 images per product (application-level rule).

---

## cart

Each user has exactly one cart.

| Column | Type |
|------|------|
| cart_id | BIGINT (PK) |
| user_id | BIGINT (FK, UNIQUE) |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

---

## cart_items

| Column | Type |
|------|------|
| cart_item_id | BIGINT (PK) |
| cart_id | BIGINT (FK) |
| product_id | BIGINT (FK) |
| quantity | INT |
| added_at | TIMESTAMP |

---

## orders

| Column | Type |
|------|------|
| order_id | BIGINT (PK) |
| buyer_id | BIGINT (FK → users) |
| total_amount | DECIMAL(10,2) |
| status | ENUM (PENDING_PAYMENT, PAID, CANCELLED, REFUNDED) |
| delivery_status | ENUM (PENDING, IN_CAMPUS, DELIVERED) |
| delivery_address_id | BIGINT (FK → user_addresses) |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

---

## order_items

| Column | Type |
|------|------|
| order_item_id | BIGINT (PK) |
| order_id | BIGINT (FK) |
| product_id | BIGINT (FK) |
| seller_id | BIGINT (FK) |
| unit_price | DECIMAL(10,2) |
| quantity | INT |
| subtotal | DECIMAL(10,2) |

---

## payments

| Column | Type |
|------|------|
| payment_id | BIGINT (PK) |
| order_id | BIGINT (FK, UNIQUE) |
| provider_reference | VARCHAR(255) |
| amount | DECIMAL(10,2) |
| status | ENUM (PENDING, SUCCESS, FAILED, REFUNDED) |
| paid_at | TIMESTAMP |
| created_at | TIMESTAMP |

---

## chats

Each order has exactly one chat.

| Column | Type |
|------|------|
| chat_id | BIGINT (PK) |
| order_id | BIGINT (FK, UNIQUE) |
| created_at | TIMESTAMP |

---

## messages

| Column | Type |
|------|------|
| message_id | BIGINT (PK) |
| chat_id | BIGINT (FK) |
| sender_id | BIGINT (FK → users) |
| message | TEXT |
| sent_at | TIMESTAMP |

---

## reviews

| Column | Type |
|------|------|
| review_id | BIGINT (PK) |
| reviewer_id | BIGINT (FK → users) |
| target_seller_id | BIGINT (FK → users) |
| order_id | BIGINT (FK) |
| rating | INT (1–5) |
| comment | TEXT |
| created_at | TIMESTAMP |

---

## Entity Relationships Summary

- User ↔ Seller (1:1)
- Seller ↔ Product (1:N)
- Product ↔ ProductImage (1:N)
- User ↔ Cart (1:1)
- Order ↔ OrderItem (1:N)
- Order ↔ Payment (1:1)
- Order ↔ Chat (1:1)
- Chat ↔ Message (1:N)
- Order ↔ Review (1:N)
