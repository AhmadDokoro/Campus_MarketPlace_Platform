# System Architecture – Campus Marketplace Platform

The system follows a layered architecture to ensure maintainability,
scalability, and clear separation of concerns.

---

## Architecture Style

- Monolithic Spring Boot application
- Layered architecture (MVC-inspired)

---

## Layers

### Presentation Layer
- Thymeleaf templates
- Handles UI rendering and user interactions

### Controller Layer
- Handles HTTP requests
- Delegates logic to services
- No business logic allowed

### Service Layer
- Implements business rules
- Coordinates workflows
- Enforces authorization and validation

### Repository Layer
- Spring Data JPA
- Handles database access only

### Database Layer
- MySQL
- Predefined schema (no auto-generation)

---

## External Integrations

- paystack: Payment processing
- Cloudinary: Image storage
- AI Service: Product moderation and scam detection

---




- BUYER, SELLER, ADMIN roles
- Only approved sellers can list products
