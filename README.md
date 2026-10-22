# 📦 Production-Grade Inventory Management REST API

A **Spring Boot 3 REST API** for managing inventory categories and items, featuring **Stateless JWT Authentication**, **3-Tier Role-Based Access Control (RBAC)**, **JPA Relational Mapping**, **Paginated & Sorted Queries**, **Centralized JSON Error Handling**, **OpenAPI 3 (Swagger UI)** documentation, and a **97-test suite with 96%+ coverage** enforced by a **SonarQube quality gate**.

---

## ✨ Features & Architecture Highlights

- 🔐 **Stateless JWT Authentication**: Built with `io.jsonwebtoken` (JJWT 0.12.6) and a custom `OncePerRequestFilter` that validates Bearer tokens on every request.
- 🛡️ **3-Tier Role-Based Access Control (RBAC)**:
  - **`ROLE_ADMIN`**: Full permissions: create categories, create / update / **delete** items.
  - **`ROLE_STAFF`**: Inventory management: create categories, create / update items (no delete).
  - **`ROLE_USER`**: Read-only catalog access.
- 🔒 **Method-Level Security**: Write endpoints are protected with `@PreAuthorize("hasRole('ADMIN')")` and `@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")`.
- 🗄️ **JPA Relational Modeling**: `@ManyToOne` / `@OneToMany` relationship linking `Item` entities to `Category` entities.
- 📄 **Pagination & Dynamic Sorting**: Spring Data `Pageable` and `Sort` (`Page<ItemResponseDto>`); unknown sort fields return a clean `400`.
- ✅ **Fail-Fast Bean Validation**: DTO input checks using `@Valid`, `@NotBlank`, `@Size`, `@Email`, `@Min`, and `@Positive`.
- 🚨 **Global Exception Handling**: A single `@RestControllerAdvice` returns every error in the same JSON format (`400`, `401`, `403`, `404`, `405`, `409`, `415`, `500`), without leaking SQL or stack traces.
- 📚 **Interactive Swagger UI**: OpenAPI 3 docs with built-in Bearer Token authorization and a custom **light / dark theme** at `/swagger-ui.html`.
- 🧪 **Automated Testing & Quality Gate**: Unit, controller-slice, and end-to-end tests measured by **JaCoCo** and analyzed by **SonarQube**.

---

## 🛠️ Technology Stack

| Area | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.5.16 (Java 25) |
| **Security** | Spring Security 6, JJWT 0.12.6, BCrypt password encoder |
| **Persistence** | Spring Data JPA, Hibernate ORM |
| **Database** | H2 in-memory (dev) / PostgreSQL (prod) |
| **Validation** | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| **API Docs** | SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 2.8.17) |
| **Testing** | JUnit 5, Mockito, AssertJ, MockMvc, `spring-security-test` |
| **Coverage & Quality** | JaCoCo 0.8.14, SonarQube (`sonar-maven-plugin` 5.8) |
| **Build Tool** | Apache Maven 3.8+ |

---

## 📁 Project Structure

```
src/main/java/com/example/basics
├── SpringBootBasicsApplication.java   # Application entry point
├── config
│   ├── DataSeeder.java                # Seeds admin & staff users on startup (dev profile only)
│   ├── OpenApiConfig.java             # OpenAPI 3 / Bearer auth + injects the Swagger light/dark theme
│   └── SecurityConfig.java            # Security filter chain, stateless sessions, public routes
├── controller
│   ├── AuthRestController.java        # /api/v1/auth        (register & login)
│   ├── CategoryRestController.java    # /api/v1/categories  (create & read)
│   ├── ItemRestController.java        # /api/v1/items       (CRUD, pagination, sorting)
│   ├── StudentRestController.java     # /api/students       (learning module: student CRUD)
│   └── HelloController.java           # /hello, /greet      (learning module: starter endpoints)
├── dto                                # Request / response objects with validation rules
├── exception
│   ├── ErrorResponse.java             # Immutable, standardized JSON error schema
│   ├── GlobalExceptionHandler.java    # @RestControllerAdvice centralized handler
│   ├── ResourceNotFoundException.java # -> 404
│   └── DuplicateResourceException.java# -> 409
├── model                              # JPA entities: Category, Item, User, Student, Role (enum)
├── repository                         # Spring Data JPA repositories
├── security
│   ├── CustomUserDetailsService.java  # Loads users by username or email
│   ├── JwtAuthenticationEntryPoint.java # Returns 401 JSON for missing / invalid tokens
│   ├── JwtAuthenticationFilter.java   # Reads & validates the Bearer token
│   └── JwtTokenProvider.java          # Signs, parses, and validates JWTs
└── service                            # Transactional business logic (Auth, Category, Item, Student)

src/main/resources
├── application.properties             # Shared config (port, Swagger, JWT)
├── application-dev.properties         # H2 database, H2 console, .env import, seed passwords
├── application-prod.properties        # PostgreSQL via environment variables
├── data.sql                           # Sample categories & items (dev profile only)
└── static/custom-swagger              # Swagger UI light/dark theme (CSS + toggle JS)

src/test                               # 97 tests: see "Testing & Code Quality" below
```

---

## 🚀 Quick Start

### 1. Prerequisites
- **Java 25+** (JDK)
- **Maven 3.8+**

### 2. Configure Secrets (`.env`)
Copy `.env.example` to `.env` in the project root and fill in the values. `.env` is git-ignored and loaded automatically in the `dev` profile:

```properties
# Required: Base64-encoded JWT signing key (at least 256 bits), e.g. output of `openssl rand -base64 64`
JWT_SECRET=<your-base64-key>
SEED_ADMIN_PASSWORD=<choose-a-password>
SEED_STAFF_PASSWORD=<choose-a-password>
```

> `JWT_SECRET` has **no default**: without it the application will not start.
> Real environment variables, if set, take precedence over values in `.env`.

<details>
<summary>Alternative: set environment variables instead of <code>.env</code> (PowerShell)</summary>

```powershell
$env:JWT_SECRET = "<your-base64-key>"
$env:SEED_ADMIN_PASSWORD = "<choose-a-password>"
$env:SEED_STAFF_PASSWORD = "<choose-a-password>"
```
</details>

### 3. Run the Application
Run from the **project root** (so `.env` is found):
```bash
mvn spring-boot:run
```
The server starts on port **`8080`** with the `dev` profile.

### 4. Explore
| URL | Purpose |
| :--- | :--- |
| [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Swagger UI: test every endpoint interactively |
| [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | Raw OpenAPI 3 JSON |
| [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | H2 database console (dev only; JDBC URL `jdbc:h2:mem:studentdb`, user `sa`, empty password) |

> **Note:** the dev database is **in-memory**: all data (including registered users) is reset on every restart. The `admin` / `staff` accounts and the sample data from `data.sql` are recreated automatically.

---

## 👥 Accounts & Roles

In the `dev` profile, `DataSeeder.java` creates these accounts on startup. No credentials live in source control; an account is skipped (with a warning) if its password is not configured.

| Username | Email | Password | Role | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **`admin`** | `admin@inventory.com` | `SEED_ADMIN_PASSWORD` from `.env` | `ROLE_ADMIN` | Read, create, update, and **delete** |
| **`staff`** | `staff@inventory.com` | `SEED_STAFF_PASSWORD` from `.env` | `ROLE_STAFF` | Read, create, and update (no delete) |
| *New registrations* | *(custom)* | *(custom)* | `ROLE_USER` | Read-only |

> 🔐 **Registration always assigns `ROLE_USER`.** A client cannot choose its own role: any extra fields such as `"roles"` in the register or login request are ignored. This prevents anyone from registering themselves as an admin. Log in as **`admin`** to perform admin actions.

---

## 📋 API Reference

All endpoints except authentication and API docs require the header `Authorization: Bearer <token>`.

### 1. Authentication (Public)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user (always `ROLE_USER`) and receive a JWT | Public |
| `POST` | `/api/v1/auth/login` | Log in with username **or** email and receive a JWT | Public |

### 2. Categories
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | List all categories (with item counts) | Authenticated |
| `GET` | `/api/v1/categories/{id}` | Get category by ID | Authenticated |
| `POST` | `/api/v1/categories` | Create a category | `ADMIN`, `STAFF` |

### 3. Items
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/items` | List items (paginated & sorted) | Authenticated |
| `GET` | `/api/v1/items/{id}` | Get item by ID | Authenticated |
| `GET` | `/api/v1/items/category/{categoryId}` | List items of a category (paginated & sorted) | Authenticated |
| `POST` | `/api/v1/items` | Create an item | `ADMIN`, `STAFF` |
| `PUT` | `/api/v1/items/{id}` | Update an item (name, price, quantity, category; SKU is unchanged) | `ADMIN`, `STAFF` |
| `DELETE` | `/api/v1/items/{id}` | Delete an item | `ADMIN` only |

**Pagination & sorting query parameters** (both list endpoints):

| Parameter | Default | Example |
| :--- | :--- | :--- |
| `page` | `0` | `page=1` |
| `size` | `10` | `size=20` |
| `sortBy` | `name` | `sortBy=price` |
| `sortDir` | `asc` | `sortDir=desc` |

### 4. Learning Modules
Kept from the earlier Spring Boot basics phase; they require any authenticated user.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` / `POST` | `/api/students` | List / create students |
| `GET` / `PUT` / `DELETE` | `/api/students/{id}` | Get / update / delete a student |
| `GET` | `/hello` | Welcome message |
| `GET` | `/greet?name=Khushi` | Personalized greeting |

---

## 🚨 Error Responses

Every error, including security errors, uses the same JSON structure:

```json
{
  "timestamp": "2026-10-22T16:58:53.328",
  "status": 403,
  "error": "Forbidden - Access Denied",
  "message": "You do not have the required permissions to perform this operation.",
  "details": ["Access Denied"]
}
```

| Status | When |
| :--- | :--- |
| `400` | Validation failed, malformed JSON, wrong parameter type (e.g. `/items/abc`), unknown sort field |
| `401` | Missing / invalid / expired token, or wrong login credentials |
| `403` | Logged in, but the role is not allowed (e.g. `STAFF` deleting an item) |
| `404` | Resource ID not found, or unknown URL |
| `405` | HTTP method not supported on that endpoint |
| `409` | Duplicate username, email, category name, or item SKU |
| `415` | Wrong `Content-Type` (use `application/json`) |
| `500` | Unexpected error: a generic message is returned; details are only logged |

---

## 🧪 Testing with cURL / Postman

### 1. Log In to Get a JWT
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "<your-admin-password>"}'
```
Check that the response contains `"roles": ["ROLE_ADMIN"]`, then copy `accessToken`.

### 2. Call a Protected Endpoint
```bash
curl -X GET "http://localhost:8080/api/v1/items?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 3. Create an Item (ADMIN or STAFF)
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

> **Using Swagger UI?** Click **Authorize 🔒**, paste only the token (Swagger adds `Bearer` for you). When switching users, click **Logout** first and authorize again with the new token. A `403` usually means you are still using a `ROLE_USER` token.

---

## ✅ Testing & Code Quality

### Run the Tests
```bash
mvn clean verify
```
This runs all **97 tests** and writes a JaCoCo coverage report to `target/site/jacoco/index.html` (open it in a browser to see covered lines in green and missed lines in red).

The tests need **no `.env` file and no real secrets**: they use a random JWT key generated in memory on every run, and an isolated H2 database (`src/test/resources/application-test.properties`).

### What Is Tested
| Layer | Test classes | Type | What is verified |
| :--- | :--- | :--- | :--- |
| **Services** | `ItemServiceTest`, `CategoryServiceTest`, `StudentServiceTest`, `AuthServiceTest` | Unit (Mockito) | Business rules, not-found and duplicate cases, password hashing, default `ROLE_USER` |
| **Security** | `JwtTokenProviderTest`, `JwtAuthenticationFilterTest`, `CustomUserDetailsServiceTest` | Unit | Valid, expired, tampered, unsigned, malformed, and wrong-key tokens |
| **Errors** | `GlobalExceptionHandlerTest` | Unit | Correct status codes; no database or internal details leaked |
| **Startup data** | `DataSeederTest` | Unit | Seed users created, skipped if they exist or have no password |
| **Controllers** | `ItemRestControllerTest`, `CategoryRestControllerTest`, `AuthRestControllerTest`, `StudentRestControllerTest`, `HelloControllerTest` | `@WebMvcTest` slice | Status codes `200`–`415` and role rules with the real security config |
| **Full application** | `ApplicationIntegrationTest` | `@SpringBootTest` (end to end) | Register → login → token → CRUD against H2, 401/403/404 flows, Swagger theme injection |

### Coverage
- Measured by **JaCoCo**; current coverage is **~96%**.
- DTOs, JPA entities, and the main class are excluded from coverage (`sonar.coverage.exclusions` in `pom.xml`) because they contain no logic.

### SonarQube Analysis
The project is configured for a local SonarQube server at `http://localhost:9001` (project key `Crud`); all settings live in `pom.xml`.

1. Start SonarQube (Docker), for example:
   ```bash
   docker run -d --name sonarqube -p 9001:9000 sonarqube:community
   ```
   If the container already exists, use `docker start <container-name>` instead. Wait until [http://localhost:9001/api/system/status](http://localhost:9001/api/system/status) shows `"status":"UP"`.
2. Run the analysis (the token is never committed; pass it via the `SONAR_TOKEN` environment variable or `~/.m2/settings.xml`):
   ```bash
   mvn clean verify sonar:sonar
   ```
3. Open the dashboard: [http://localhost:9001/dashboard?id=Crud](http://localhost:9001/dashboard?id=Crud)

**Quality gate status: ✅ Passed**: coverage on new code 96.7% (required ≥ 80%), 0 issues, 0 duplicated new lines, 0 security hotspots.

---

## 🏭 Running in Production (PostgreSQL)

Activate the `prod` profile and provide configuration through **environment variables** (`.env` is only read in `dev`):

| Variable | Required | Default |
| :--- | :--- | :--- |
| `JWT_SECRET` | ✅ Yes | *(none: app will not start without it)* |
| `JWT_EXPIRATION` | No | `86400000` (24 hours) |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/studentdb` |
| `DB_USERNAME` | No | `postgres` |
| `DB_PASSWORD` | Recommended | `postgres` |

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:JWT_SECRET = "<your-base64-key>"
$env:DB_PASSWORD = "<your-db-password>"
mvn spring-boot:run
```

Differences from `dev`:
- The schema is created / updated automatically (`ddl-auto=update`), and data persists across restarts.
- The H2 console is disabled, and `data.sql` sample data is **not** loaded.
- **No users are seeded** (`DataSeeder` runs only in `dev`). Because registration always assigns `ROLE_USER`, the first `ROLE_ADMIN` account must be created directly in the database (BCrypt-hashed password, role in the `user_roles` table).

---

## 🔒 Security Standards (12-Factor App)

- **No secrets in source control**: `JWT_SECRET` and seed passwords come from environment variables or the git-ignored `.env`; the SonarQube token comes from `SONAR_TOKEN` / `settings.xml`.
- **No secret fallbacks**: `app.jwt.secret=${JWT_SECRET}` has no default, so a missing key fails fast at startup instead of silently using a weak key.
- **Passwords hashed with BCrypt**: plain-text passwords are never stored.
- **Stateless sessions**: no server-side session or cookies; CSRF protection is unnecessary because authentication uses only the `Authorization` header.
- **Safe error messages**: database and internal exception details are logged, never returned to clients.
