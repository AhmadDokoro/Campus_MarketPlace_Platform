# System Modules – Campus Marketplace Platform

This document defines system modules and their boundaries.

---

## 1. Manage Seller Verification

Actors:
- Seller
- Admin

Functions:
- Seller submits verification request
- Admin approves or rejects seller
- Seller status updates accordingly

Rules:
- Seller must be APPROVED before listing products

---

## 2. Manage System Activities

Actors:
- Admin

Functions:
- View system statistics
- Review AI-flagged products

---

## 3. Explore Products

Actors:
- Buyer

Functions:
- Browse products
- Search products
- Filter products
- View product details
- Add products to cart

---

## 4. Manage Orders

Actors:
- Buyer
- Seller

Functions:
- Place order
- Make payment
- Track delivery status
- Open system chat
- Review seller

Rules:
- Chat opens only after payment
- Each order has one chat

---

## 5. Manage Product Listings

Actors:
- Seller
- Admin

Functions:
- Add product
- Edit product
- Delete product
- Manage categories (Admin)

Rules:
- Only approved sellers can list products
- Max 2 images per product

---

## 6. Manage Sales

Actors:
- Seller

Functions:
- View sales history
- Receive payment notifications
- Update delivery status
