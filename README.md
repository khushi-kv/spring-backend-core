# 📦 Production-Grade Inventory Management REST API

A modern, enterprise-standard **Spring Boot 3 REST API** featuring **Stateless JWT Authentication**, **3-Tier Role-Based Access Control (RBAC)**, **JPA Relational Mapping**, **Paginated & Sorted Queries**, **Centralized Error Handling**, and **OpenAPI 3 (Swagger UI)** documentation.

---

## ✨ Features & Architecture Highlights

- 🔐 **Stateless JWT Authentication**: Built with `io.jsonwebtoken` (JJWT 0.12.6) and a custom `OncePerRequestFilter` to validate Bearer tokens.
- 🛡️ **3-Tier Role-Based Access Control (RBAC)**:
  - **`ROLE_ADMIN`**: Full permissions (Create, Update, and **Delete** inventory items & categories).
  - **`ROLE_STAFF`**: Inventory management permissions (Create & Update items/categories, No Delete).
  - **`ROLE_USER`**: Read-only catalog viewer permissions.
- 🔒 **Method-Level Security**: Protected endpoints using `@PreAuthorize("hasRole('ADMIN')")` and `@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")`.
- 🗄️ **JPA Relational Modeling**: Clean `@ManyToOne` / `@OneToMany` relationships linking `Item` entities to `Category` entities.
- 📄 **Pagination & Dynamic Sorting**: High-performance querying using Spring Data `Pageable` and `Sort` (`Page<ItemResponseDto>`).
- ✅ **Fail-Fast Bean Validation**: DTO input sanitization using `@Valid`, `@NotBlank`, `@Size`, `@Min`, and `@Positive`.
- 🚨 **Global Exception Handling**: Centralized `@RestControllerAdvice` returning structured RFC 7807-style error responses for `400 Bad Request`, `403 Forbidden`, `404 Not Found`, and `500 Internal Server Error`.
- 📚 **Interactive Swagger UI**: Full OpenAPI 3 documentation with built-in Bearer Token authorization at `/swagger-ui.html`.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.3.2 (Java 17)
- **Security**: Spring Security 6, JJWT (v0.12.6), BCrypt Password Encoder
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Database**: H2 In-Memory DB (Development) / PostgreSQL (Production)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **API Documentation**: SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 2.6.0)
- **Build Tool**: Apache Maven

---

## 📁 Package Architecture

```
com.example.basics
├── config
│   ├── DataSeeder.java         # Auto-seeds Admin & Staff users on startup
│   ├── OpenApiConfig.java      # OpenAPI 3 / Swagger UI config with Bearer Auth
│   └── SecurityConfig.java     # Spring Security Filter Chain & Stateless session policy
├── controller
│   ├── AuthRestController.java     # /api/v1/auth (Register & Login)
│   ├── CategoryRestController.java # /api/v1/categories (Category CRUD)
│   └── ItemRestController.java     # /api/v1/items (Item CRUD, Pagination, Sorting)
├── dto
│   ├── AuthRequestDto.java, AuthResponseDto.java, RegisterRequestDto.java
│   ├── CategoryRequestDto.java, CategoryResponseDto.java
│   └── ItemRequestDto.java, ItemResponseDto.java
├── exception
│   ├── ErrorResponse.java          # Standardized JSON error schema
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice centralized handler
│   └── ResourceNotFoundException.java
├── model
│   ├── Category.java, Item.java, Role.java, User.java
├── repository
│   ├── CategoryRepository.java, ItemRepository.java, UserRepository.java
├── security
│   ├── CustomUserDetailsService.java # Bridges User entity with Spring Security
│   ├── JwtAuthenticationFilter.java  # Intercepts & validates Bearer tokens
│   └── JwtTokenProvider.java        # Signs, parses, and validates JWTs
└── service
    ├── AuthService.java        # Registration, BCrypt hashing, JWT issuance
    ├── CategoryService.java     # Transactional Category business logic
    └── ItemService.java         # Transactional Item business logic & pagination
```

---

## 🚀 Quick Start

### 1. Prerequisites
- **Java 17+** (JDK)
- **Maven 3.8+** (or use `./mvnw`)

### 2. Run the Application
```bash
mvn spring-boot:run
```
The server will start on port `8080`.

### 3. Open Swagger UI
Navigate to **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** to test all endpoints interactively.

---

## 👥 Default Demo Credentials (Pre-Seeded)

On application startup, `DataSeeder.java` and `data.sql` automatically seed the following test accounts:

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `ROLE_ADMIN` | Full Read, Write, and **Delete** access |
| **`staff`** | `staff123` | `ROLE_STAFF` | Read, Create, and Update access (No Delete) |
| *New Registrations* | *(custom)* | `ROLE_USER` | Read-only catalog access |

---

## 📋 API Reference

### 1. Authentication Endpoints (Public)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user (`ROLE_USER`) | Public |
| `POST` | `/api/v1/auth/login` | Authenticate credentials & receive JWT | Public |

### 2. Category Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | List all categories | Authenticated |
| `GET` | `/api/v1/categories/{id}` | Get category by ID | Authenticated |
| `POST` | `/api/v1/categories` | Create new category | `ADMIN`, `STAFF` |

### 3. Item Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/items` | List items (Paginated & Sorted) | Authenticated |
| `GET` | `/api/v1/items/{id}` | Get item by ID | Authenticated |
| `GET` | `/api/v1/items/category/{id}` | List items by category | Authenticated |
| `POST` | `/api/v1/items` | Create new inventory item | `ADMIN`, `STAFF` |
| `PUT` | `/api/v1/items/{id}` | Update existing item | `ADMIN`, `STAFF` |
| `DELETE` | `/api/v1/items/{id}` | Delete item by ID | `ADMIN` Only |

---

## 🧪 Testing with cURL / Postman

### 1. Login to Get JWT Token
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "admin123"}'
```

### 2. Access Protected Endpoint with Bearer Token
```bash
curl -X GET "http://localhost:8080/api/v1/items?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 3. Create Item (Requires ADMIN or STAFF)
```bash
curl -X POST "http://localhost:8080/api/v1/items" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mechanical Keyboard",
    "sku": "KEY-2026",
    "price": 129.99,
    "quantity": 45,
    "categoryId": 1
  }'
```

---

## 🔒 Configuration & Security Standards (12-Factor App)

Environment properties in `application.properties` use secure fallback expressions:
```properties
app.jwt.secret=${JWT_SECRET:<your_base64_secret_key>}
app.jwt.expiration-ms=${JWT_EXPIRATION:86400000}
```
* **Local Dev**: Automatically uses the built-in development key.
* **Production Deployment**: Securely injects `JWT_SECRET` from environment variables without hardcoding credentials in GitHub.

