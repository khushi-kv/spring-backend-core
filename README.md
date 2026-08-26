# 🍃 Spring Boot Basics & Database Persistence

A Production-grade Spring Boot REST API demonstrating core backend concepts including Controllers, DTOs, Bean Validation, Business Logic Services, Spring Data JPA Repositories, Entities, Global Exception Handling (`@RestControllerAdvice`), and H2 Database integration.

## 📁 Project Structure

- **`com.example.basics.SpringBootBasicsApplication`**: Main entry point for the Spring Boot application.
- **`com.example.basics.controller.HelloController`**: Simple "Hello World" REST endpoints (`/hello`, `/greet`).
- **`com.example.basics.controller.StudentRestController`**: Full REST API CRUD endpoints (`/api/students`).
- **`com.example.basics.dto.StudentRequestDto`**: Incoming request DTO with `@NotBlank` and `@Size` validations.
- **`com.example.basics.dto.StudentResponseDto`**: Outgoing response DTO hiding DB internal fields.
- **`com.example.basics.exception.GlobalExceptionHandler`**: `@RestControllerAdvice` handling custom 404, 400 validation errors, and 500 fallbacks.
- **`com.example.basics.service.StudentService`**: Business logic layer.
- **`com.example.basics.repository.StudentRepository`**: Data access layer extending `JpaRepository<Student, Long>`.
- **`com.example.basics.model.Student`**: Relational entity model mapped with `@Entity`, `@Id`, `@GeneratedValue`.

## 🗄️ Database & Persistence

- **Database Engine**: H2 In-Memory Database (`jdbc:h2:mem:studentdb`)
- **ORM Framework**: Hibernate / Spring Data JPA
- **H2 Web Console**: Accessible at `http://localhost:8080/h2-console`
  - **JDBC URL**: `jdbc:h2:mem:studentdb`
  - **Username**: `sa`
  - **Password**: *(leave blank)*

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.x

### Run Command
```bash
mvn spring-boot:run
```

### Endpoints to Test

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | Get all students | `200 OK` |
| `GET` | `/api/students/{id}` | Get student by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/students` | Create new student | `201 Created` / `400 Bad Request` |
| `PUT` | `/api/students/{id}` | Update existing student | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/students/{id}` | Delete student by ID | `204 No Content` / `404 Not Found` |

### Sample Payloads

**POST / PUT Request Payload:**
```json
{
  "name": "Khushi",
  "course": "Spring Boot Foundations"
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-08-14T18:58:30",
  "status": 400,
  "error": "Bad Request - Validation Failed",
  "message": "Input validation failed for one or more fields",
  "details": [
    "name: Student name cannot be empty"
  ]
}
```
