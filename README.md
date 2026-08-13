# 🍃 Spring Boot Basics & Database Persistence

A beginner-friendly Spring Boot REST API project demonstrating core backend concepts including Controllers, Services, Spring Data JPA Repositories, Entities, and H2 Database integration.

## 📁 Project Structure

- **`com.example.basics.SpringBootBasicsApplication`**: Main entry point for the Spring Boot application.
- **`com.example.basics.controller.HelloController`**: Simple "Hello World" REST endpoints (`/hello`, `/greet`).
- **`com.example.basics.controller.StudentRestController`**: REST API endpoints for managing students (`/api/students`).
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
- `GET  http://localhost:8080/hello`
- `GET  http://localhost:8080/greet?name=Khush`
- `GET  http://localhost:8080/api/students`
- `GET  http://localhost:8080/api/students/1`
- `POST http://localhost:8080/api/students` (Payload: `{"name": "joy", "course": "Spring Boot"}`)

