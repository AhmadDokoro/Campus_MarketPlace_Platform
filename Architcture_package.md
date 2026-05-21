# Campus Marketplace Platform Architecture Summary

## 1. Project Overview

- Project name: `campusmarket`
- Group/package root: `com.ahsmart.campusmarket`
- Main application class: `CampusmarketApplication`
- Main purpose: student-to-student campus marketplace for UMT students to buy, sell, review, and track products.
- Application style: Spring Boot MVC web application with Thymeleaf views, Spring Data JPA persistence, and session-based role checks.

## 2. Key Modules and Features

- Authentication and session login
- User profile and address management
- Seller verification and seller dashboard
- Product listing, product search, product detail, category browsing
- Cart and checkout
- Order tracking and seller delivery status updates
- Mock online payment flow
- Buyer-seller chat after paid orders
- Product and seller reviews
- Admin dashboard, seller approval, category management, mentor management, flagged product moderation
- External image upload/search and AI fraud detection

## 3. Package Structure

```text
com.ahsmart.campusmarket
|-- config
|-- controller
|-- docs
|-- exceptions
|-- helper
|-- model
|   `-- enums
|-- payloadDTOs
|   |-- AuthenticationDTOs
|   |-- admin
|   |-- chat
|   |-- order
|   |-- review
|   `-- user
|-- repositories
`-- service
    |-- admin
    |-- authentication
    |-- cart
    |-- category
    |-- chat
    |-- mentor
    |-- openai
    |-- order
    |-- payment
    |-- product
    |-- review
    `-- user
```

## 4. Resource Structure

```text
src/main/resources
|-- application.properties
|-- db
|-- static
|   |-- animation
|   |-- css
|   |-- img
|   |-- js
|   `-- lib
`-- templates
    |-- admin
    |-- auth
    |-- buyer
    |-- cart
    |-- chat
    |-- fragments
    |-- order
    |-- product-listings
    |-- seller
    `-- user
```

## 5. Layered Architecture

- Architecture pattern: Spring MVC layered architecture.
- Presentation layer: `controller`, `templates`, `static`
- Business/service layer: `service.*`
- Persistence layer: `repositories`
- Domain layer: `model`, `model.enums`
- Transfer objects: `payloadDTOs.*`
- Cross-cutting support: `config`, `exceptions`, `helper`

## 6. Package Dependency Map

```text
controller
  -> service.*
  -> model, model.enums
  -> payloadDTOs.*
  -> repositories.SellerRepository in AuthenticationController

service.*
  -> repositories
  -> model, model.enums
  -> payloadDTOs.*
  -> helper
  -> service.* where workflows cross modules

repositories
  -> model, model.enums
  -> payloadDTOs.* for query projections

model
  -> model.enums

helper
  -> payloadDTOs.review
  -> service.review
  -> repositories.ReviewRepository
  -> external libraries/config APIs

config
  -> Cloudinary
  -> ModelMapper
  -> Spring MVC exception handling

exceptions
  -> payloadDTOs.APIExceptionResponse
```

## 7. Main Layer Flow

```text
Browser/User
  -> Controller
  -> Service
  -> Repository
  -> JPA Entity
  -> Database
```

```text
AJAX/JSON request
  -> Controller @ResponseBody
  -> Service
  -> Repository
  -> DTO response
  -> Browser
```

## 8. Feature Module Grouping

### Authentication

- Controllers: `AuthenticationController`
- Services: `service.authentication`
- DTOs: `payloadDTOs.AuthenticationDTOs`
- Models: `Users`, `Seller`, `Mentor`, `Role`, `SellerStatus`
- Repositories: `UsersRepository`, `SellerRepository`, `MentorRepository`
- Notes: login stores `userId`, `userName`, and `role` in `HttpSession`.

### User Management

- Controllers: `UserController`
- Services: `service.user`
- DTOs: `payloadDTOs.user`
- Models: `Users`, `UserAddress`, `Seller`
- Repositories: `UsersRepository`, `UserAddressRepository`, `SellerRepository`
- Features: profile view/update, address data, start-selling routing decision.

### Seller Module

- Controllers: `ProductController`, parts of `AuthenticationController`
- Services: `service.product`, `service.order`, `service.user`
- Models: `Seller`, `Product`, `ProductImage`, `OrderItem`
- Repositories: `SellerRepository`, `ProductRepository`, `ProductImageRepository`, `OrderItemRepository`
- Features: seller verification, product CRUD, dashboard metrics, sales history, delivery status updates.

### Product Module

- Controllers: `ProductController`, `ExploreProductsController`
- Services: `service.product`, `service.category`, `service.openai`
- DTOs: `payloadDTOs.review`, `payloadDTOs.admin`
- Models: `Product`, `ProductImage`, `Category`, `Review`, product-related enums
- Repositories: `ProductRepository`, `ProductImageRepository`, `CategoryRepository`, `ReviewRepository`
- Features: listing creation/edit/delete, product browse/search, product detail, related products, image upload, AI fraud flagging.

### Category Module

- Controllers: `CategoryController`
- Services: `service.category`
- Models: `Category`, `Product`
- Repositories: `CategoryRepository`, `ProductRepository`
- Features: admin category CRUD, homepage category data.

### Cart Module

- Controllers: `CartController`
- Services: `service.cart`
- Models: `Cart`, `CartItem`, `Product`, `Users`
- Repositories: `CartRepository`, `CartItemRepository`, `ProductRepository`, `UsersRepository`
- Features: add/update/remove cart items, cart count, cart total.

### Order System

- Controllers: `OrderController`, seller order endpoints in `ProductController`
- Services: `service.order`, `service.cart`, `service.payment`, `service.chat`
- DTOs: `payloadDTOs.order`
- Models: `Order`, `OrderItem`, `Payment`, `UserAddress`, `Product`, `Seller`
- Repositories: `OrderRepository`, `OrderItemRepository`, `PaymentRepository`, `UserAddressRepository`, `ProductRepository`
- Features: checkout, order creation from cart, buyer history/detail, buyer tracking counts, seller order list, delivery status transitions.

### Payment System

- Controllers: `OrderController`
- Services: `PaymentService`, `MockToyyibPayService`
- Models: `Payment`, `Order`
- Repositories: `PaymentRepository`, `OrderRepository`
- Features: pending payment creation, mock card validation, mock gateway processing, payment/order status updates.

### Chat System

- Controllers: `ChatController`
- Services: `service.chat`
- DTOs: `payloadDTOs.chat`
- Models: `Chat`, `Message`, `Order`, `OrderItem`, `Users`
- Repositories: `ChatRepository`, `MessageRepository`, `OrderItemRepository`, `UsersRepository`
- Features: one chat per paid order item, participant authorization, message send/list APIs.
- Implementation: HTTP MVC endpoints and browser `fetch`; no WebSocket/STOMP layer detected.

### Review System

- Controllers: `ReviewController`
- Services: `service.review`
- Helpers: `ProductRatingHelper`, `SellerRatingHelper`
- DTOs: `payloadDTOs.review`
- Models: `Review`, `OrderItem`, `Users`, `Seller`
- Repositories: `ReviewRepository`, `OrderItemRepository`
- Features: buyer review submission after received order item, product rating summary, seller rating summary, product review modal data.

### Admin Module

- Controllers: `AdminController`, `CategoryController`
- Services: `service.admin`, `service.mentor`, `service.category`
- DTOs: `payloadDTOs.admin`
- Models: `Seller`, `Users`, `Product`, `Mentor`, `Category`
- Repositories: `SellerRepository`, `UsersRepository`, `ProductRepository`, `ProductImageRepository`, `CartItemRepository`, `OrderItemRepository`, `MentorRepository`, `CategoryRepository`
- Features: admin dashboard analytics, seller verification approval/rejection, flagged product approval/deletion, mentor management, category management.

### Mentor Module

- Controllers: `AdminController`, `AuthenticationController`
- Services: `service.mentor`
- Models: `Mentor`, `Users`
- Repositories: `MentorRepository`, `UsersRepository`
- Features: mentor CRUD by admin, mentor selection during registration.

## 9. Special Components

### Security and Authentication

- Spring Security: not detected.
- JWT usage: not detected.
- Authentication style: custom session-based login with `HttpSession`.
- Role handling: `Role` enum stored in session and checked manually in controllers/services.

### WebSocket and Chat

- WebSocket dependency/configuration: not detected.
- STOMP/SockJS annotations: not detected.
- Chat implementation: REST-style MVC endpoints under `/chat` with persisted `Chat` and `Message` entities.

### Configuration Classes

- `AppConfig`: defines `ModelMapper` and `Cloudinary` beans.
- `AppConstants`: application constants.
- `GlobalExceptionHandler`: handles upload-size and generic MVC errors.
- `OpenAiConfig`: OpenAI endpoint/model/API-key configuration.
- `myGlobalExceptionHandler`: handles validation, resource-not-found, and API exceptions.

### Startup Schema Compatibility Services

- `ChatSchemaCompatibilityService`: startup schema compatibility checks for chat-related database structure.
- `OrderItemDeliveryStatusSchemaCompatibilityService`: startup schema compatibility checks for order item delivery status support.

### External Integrations

- Cloudinary: image upload, remote image upload, image deletion, thumbnail URL helper.
- OpenAI Chat Completions API: product fraud detection through `OpenAiService`.
- Pexels API: seller product image search through `ProductController` using `RestTemplate`.
- Gmail SMTP/Jakarta Mail: seller verification notification email through `EmailHelper`.
- Mock ToyyibPay: local mock payment gateway through `MockToyyibPayService`.
- MySQL: primary configured database through Spring Data JPA/Hibernate.
- H2: runtime dependency present, mainly useful for local/test scenarios.

## 10. Important Runtime Flows

### Login Flow

```text
User submits login
  -> AuthenticationController
  -> AuthenticationService
  -> UsersRepository / SellerRepository
  -> HttpSession role and user data
  -> Redirect by role
```

### Product Creation Flow

```text
Seller submits product form
  -> ProductController
  -> ProductService
  -> OpenAiService for fraud detection
  -> FileService / Cloudinary for image storage
  -> ProductRepository / ProductImageRepository
  -> Product and ProductImage persisted
```

### Checkout and Payment Flow

```text
Buyer checks out cart
  -> OrderController
  -> OrderService
  -> CartService + ProductRepository + OrderRepository
  -> PaymentService for online payment
  -> MockToyyibPayService
  -> PaymentRepository / OrderRepository
  -> ChatService creates chats after successful payment
```

### Chat Flow

```text
Buyer or seller opens chat
  -> ChatController
  -> ChatService
  -> ChatRepository / MessageRepository
  -> chat.html renders messages
  -> Browser sends/loads messages with fetch endpoints
```

### Admin Moderation Flow

```text
Admin reviews seller or flagged product
  -> AdminController
  -> AdminService
  -> SellerRepository / ProductRepository / UsersRepository
  -> optional EmailHelper notification
  -> database state updated
```

## 11. Domain Model Summary

- User/account: `Users`, `Role`, `Mentor`
- Seller verification: `Seller`, `SellerStatus`
- Product catalog: `Product`, `ProductImage`, `Category`, `Condition`, `ProductStatus`, `FlaggedStatus`
- Cart: `Cart`, `CartItem`
- Orders: `Order`, `OrderItem`, `OrderStatus`, `DeliveryStatus`, `PaymentMethod`
- Payment: `Payment`, `PaymentStatus`
- Chat: `Chat`, `Message`
- Reviews: `Review`
- Address: `UserAddress`

## 12. UML Package Diagram Notes

- Main dependency direction is `controller -> service -> repositories -> model`.
- `payloadDTOs` is shared by controllers, services, repositories, and helpers.
- `helper` is not purely utility-only; some helpers depend on services or repositories for rating data.
- `AuthenticationController` directly depends on `SellerRepository`, which is an exception to the main layered dependency direction.
- Chat is a persistence-backed MVC feature, not a WebSocket package.
- Security should be represented as custom session/role handling, not as Spring Security/JWT packages.
