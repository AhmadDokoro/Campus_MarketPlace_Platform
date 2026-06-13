# Campus Marketplace — UML Package Diagram Data

Base package: `com.ahsmart.campusmarket`
Architecture style: **technical layering** (packages are split by layer — `controller`, `service`, `repositories`, `model` — not by domain). Service layer is further sub-packaged by domain.

---

## 1. Complete Package Hierarchy

```
com.ahsmart.campusmarket
├── (root)            CampusmarketApplication
├── config
├── controller
├── exceptions
├── helper
├── model
│   └── enums
├── payloadDTOs
│   ├── AuthenticationDTOs
│   ├── admin
│   ├── chat
│   ├── order
│   ├── review
│   └── user
├── repositories
└── service
    ├── admin
    ├── authentication
    ├── cart
    ├── category
    ├── chat
    ├── embedding
    ├── mentor
    ├── openai
    ├── order
    ├── payment
    ├── product
    ├── recommendation
    ├── report
    ├── review
    └── user
```

---

## 2. Immediate Subpackages

```
model
└── enums

payloadDTOs
├── AuthenticationDTOs
├── admin
├── chat
├── order
├── review
└── user

service
├── admin
├── authentication
├── cart
├── category
├── chat
├── embedding
├── mentor
├── openai
├── order
├── payment
├── product
├── recommendation
├── report
├── review
└── user
```

`config`, `controller`, `exceptions`, `helper`, `repositories` have **no** subpackages.

---

## 3. Main Classes Per Package

**config**
- AppConfig
- AppConstants
- GlobalExceptionHandler

**controller**
- AdminController
- AuthenticationController
- CartController
- CategoryController
- ChatController
- ExploreProductsController
- OrderController
- ProductController
- ReviewController
- UserController

**exceptions**
- APIException
- ResourceNotFoundException
- myGlobalExceptionHandler

**helper**
- CloudinaryUrlHelper
- EmailHelper
- OpenAiConfig
- ProductRatingHelper
- SellerRatingHelper

**model**
- Cart, CartItem, Category, Chat, Mentor, Message, Order, OrderItem, Payment, Product, ProductImage, Review, Seller, UserAddress, Users

**model.enums**
- Condition, DeliveryStatus, FlaggedStatus, OrderStatus, PaymentMethod, PaymentStatus, ProductStatus, Role, SellerStatus

**payloadDTOs**
- APIExceptionResponse
- AuthenticationDTOs: LoginResult
- admin: CategoryStatsDTO, FlaggedProductDTO, SellerStatDTO, WeeklyListingDTO
- chat: ChatDTO, MessageDTO
- order: BuyerOrderItemChatDTO, BuyerOrderTrackingSummaryDTO, SellerOrderItemDTO, SellerSalesHistoryDTO
- review: ProductRatingData, ProductReviewDTO, SellerRatingData
- user: UserProfileFormDTO

**repositories**
- CartItemRepository, CartRepository, CategoryRepository, ChatRepository, MentorRepository, MessageRepository, OrderItemRepository, OrderRepository, PaymentRepository, ProductImageRepository, ProductRepository, ReviewRepository, SellerRepository, UserAddressRepository, UsersRepository

**service.admin**
- AdminService, AdminServiceImpl

**service.authentication**
- AuthenticationService, AuthenticationServiceImpl

**service.cart**
- CartService, CartServiceImpl

**service.category**
- CategoryService, categoryServiceImpl

**service.chat**
- ChatService, ChatServiceImpl, ChatSchemaCompatibilityService

**service.embedding**
- EmbeddingService, EmbeddingServiceImpl

**service.mentor**
- MentorService, MentorServiceImpl

**service.openai**
- OpenAiService, OpenAiServiceImpl

**service.order**
- OrderService, OrderServiceImpl, OrderItemDeliveryStatusSchemaCompatibilityService

**service.payment**
- PaymentService, MockToyyibPayService

**service.product**
- ProductService, ProductServiceImpl, FileService, FileServiceImpl

**service.recommendation**
- RecommendationService, RecommendationServiceImpl

**service.report**
- AdminReportService, AdminReportData, AdminReportPeriod

**service.review**
- ReviewService, ReviewServiceImpl

**service.user**
- UserService, UserServiceImpl, StartSellingDecision

---

## 4. Package Dependencies (derived from actual imports)

```
controller
→ service.* (all domains)
→ model, model.enums
→ payloadDTOs.*
→ repositories (AuthenticationController → SellerRepository only)

service.admin
→ repositories (CartItem, OrderItem, ProductImage, Product, Seller, Users)
→ service.product (FileService)
→ helper (EmailHelper)
→ model, model.enums, payloadDTOs.admin

service.authentication
→ repositories (Mentor, Seller, Users)
→ service.product (FileService)
→ model, model.enums, payloadDTOs.AuthenticationDTOs

service.cart
→ repositories (CartItem, Cart, Product, Users)
→ model

service.category
→ repositories (Category)
→ model

service.chat
→ repositories (Chat, Message, OrderItem, Users)
→ model, payloadDTOs.chat

service.embedding
→ repositories (Product)
→ helper (OpenAiConfig)
→ model

service.mentor
→ repositories (Mentor, Users)
→ model

service.openai
→ helper (OpenAiConfig)
→ model.enums

service.order
→ repositories (OrderItem, Order, Product, UserAddress, Users)
→ service.cart, service.chat
→ model, model.enums, payloadDTOs.order

service.payment
→ repositories (Order, Payment)
→ service.chat
→ model, model.enums

service.product
→ repositories (CartItem, Category, OrderItem, ProductImage, Product, Seller, Users)
→ service.embedding, service.openai
→ exceptions (APIException — FileServiceImpl)
→ model, model.enums

service.recommendation
→ repositories (Product)
→ service.embedding
→ model

service.report
→ repositories (OrderItem, Product, Seller, Users)
→ model, model.enums, payloadDTOs.admin

service.review
→ repositories (OrderItem, Review)
→ model, model.enums, payloadDTOs.review

service.user
→ repositories (Seller, UserAddress, Users)
→ model, model.enums, payloadDTOs.user

repositories
→ model, model.enums, payloadDTOs.order, payloadDTOs.review

helper
→ service.review (ProductRatingHelper)
→ repositories (SellerRatingHelper → ReviewRepository)
→ payloadDTOs.review
```

---

## 5. Dependency Table

| Source Package | Depends On Package |
|---|---|
| controller | service.admin |
| controller | service.authentication |
| controller | service.cart |
| controller | service.category |
| controller | service.chat |
| controller | service.embedding |
| controller | service.mentor |
| controller | service.order |
| controller | service.payment |
| controller | service.product |
| controller | service.recommendation |
| controller | service.report |
| controller | service.review |
| controller | service.user |
| controller | repositories |
| controller | model |
| controller | model.enums |
| controller | payloadDTOs.admin |
| controller | payloadDTOs.chat |
| controller | payloadDTOs.order |
| controller | payloadDTOs.review |
| controller | payloadDTOs.user |
| controller | payloadDTOs.AuthenticationDTOs |
| service.admin | repositories |
| service.admin | service.product |
| service.admin | helper |
| service.admin | payloadDTOs.admin |
| service.authentication | repositories |
| service.authentication | service.product |
| service.authentication | payloadDTOs.AuthenticationDTOs |
| service.cart | repositories |
| service.category | repositories |
| service.chat | repositories |
| service.chat | payloadDTOs.chat |
| service.embedding | repositories |
| service.embedding | helper |
| service.mentor | repositories |
| service.openai | helper |
| service.order | repositories |
| service.order | service.cart |
| service.order | service.chat |
| service.order | payloadDTOs.order |
| service.payment | repositories |
| service.payment | service.chat |
| service.product | repositories |
| service.product | service.embedding |
| service.product | service.openai |
| service.product | exceptions |
| service.recommendation | repositories |
| service.recommendation | service.embedding |
| service.report | repositories |
| service.report | payloadDTOs.admin |
| service.review | repositories |
| service.review | payloadDTOs.review |
| service.user | repositories |
| service.user | payloadDTOs.user |
| repositories | model |
| repositories | model.enums |
| repositories | payloadDTOs.order |
| repositories | payloadDTOs.review |
| helper | service.review |
| helper | repositories |
| helper | payloadDTOs.review |
| all services | model |
| all services | model.enums |

---

## 6. Architectural Layers

**Presentation Layer**
- controller
- exceptions (GlobalExceptionHandler, myGlobalExceptionHandler — web error handling)

**Business Layer**
- service.admin, service.authentication, service.cart, service.category, service.chat, service.mentor, service.order, service.payment, service.product, service.recommendation, service.report, service.review, service.user
- service.embedding, service.openai (AI services)

**Data Access Layer**
- repositories

**Domain Layer**
- model
- model.enums

**Data Transfer Layer**
- payloadDTOs (+ AuthenticationDTOs, admin, chat, order, review, user)

**Infrastructure / Cross-Cutting Layer**
- config (AppConfig, AppConstants, GlobalExceptionHandler)
- helper (CloudinaryUrlHelper, EmailHelper, OpenAiConfig, ProductRatingHelper, SellerRatingHelper)

---

## 7. External Integration Packages

Confirmed from `pom.xml` and source:

| Integration | Where in code | Library |
|---|---|---|
| **Cloudinary** (image hosting) | helper.CloudinaryUrlHelper, service.product.FileServiceImpl | `com.cloudinary:cloudinary-http44` |
| **OpenAI** (embeddings + moderation) | helper.OpenAiConfig, service.openai, service.embedding | raw HTTP (no SDK dependency) |
| **ToyyibPay** (payment gateway — mocked) | service.payment.MockToyyibPayService, service.payment.PaymentService | local mock implementation |
| **SMTP Email** (Gmail) | helper.EmailHelper | `jakarta.mail` / `com.sun.mail` |
| **PDF generation** (admin reports) | service.report.AdminReportService | `com.github.librepdf:openpdf` |
| **Thymeleaf** (server-side rendering) | all controllers | `spring-boot-starter-thymeleaf` |

> Note: **No JWT and no Spring Security** packages exist in this repository. Authentication is session-based (`session.userId`) handled in `service.authentication`. There is no `security` package and no `payment gateway` package beyond `service.payment`.

---

## PACKAGE DIAGRAM DATA

```
PACKAGE: com.ahsmart.campusmarket
  SUBPACKAGES:
    config
    controller
    exceptions
    helper
    model
      └ enums
    payloadDTOs
      ├ AuthenticationDTOs
      ├ admin
      ├ chat
      ├ order
      ├ review
      └ user
    repositories
    service
      ├ admin
      ├ authentication
      ├ cart
      ├ category
      ├ chat
      ├ embedding
      ├ mentor
      ├ openai
      ├ order
      ├ payment
      ├ product
      ├ recommendation
      ├ report
      ├ review
      └ user

DEPENDENCIES:
  controller            → service.*
  controller            → repositories
  controller            → payloadDTOs.*
  controller            → model
  controller            → model.enums
  service.admin         → repositories
  service.admin         → service.product
  service.admin         → helper
  service.admin         → payloadDTOs.admin
  service.authentication→ repositories
  service.authentication→ service.product
  service.authentication→ payloadDTOs.AuthenticationDTOs
  service.cart          → repositories
  service.category      → repositories
  service.chat          → repositories
  service.chat          → payloadDTOs.chat
  service.embedding     → repositories
  service.embedding     → helper
  service.mentor        → repositories
  service.openai        → helper
  service.order         → repositories
  service.order         → service.cart
  service.order         → service.chat
  service.order         → payloadDTOs.order
  service.payment       → repositories
  service.payment       → service.chat
  service.product       → repositories
  service.product       → service.embedding
  service.product       → service.openai
  service.product       → exceptions
  service.recommendation→ repositories
  service.recommendation→ service.embedding
  service.report        → repositories
  service.report        → payloadDTOs.admin
  service.review        → repositories
  service.review        → payloadDTOs.review
  service.user          → repositories
  service.user          → payloadDTOs.user
  repositories          → model
  repositories          → model.enums
  repositories          → payloadDTOs.order
  repositories          → payloadDTOs.review
  helper                → service.review
  helper                → repositories
  service.*             → model
  service.*             → model.enums
  model                 → model.enums

EXTERNAL INTEGRATIONS:
  Cloudinary   (helper, service.product)
  OpenAI       (helper.OpenAiConfig, service.openai, service.embedding)
  ToyyibPay    (service.payment — mock)
  SMTP/Email   (helper.EmailHelper)
  OpenPDF      (service.report)
  Thymeleaf    (controller)
```
