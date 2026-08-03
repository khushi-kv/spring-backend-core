# 🍃 Spring Boot Basics

A beginner-friendly Spring Boot REST API project demonstrating core concepts including Controllers, Services, Repositories, and Models.

## 📁 Project Structure

- **`com.example.basics.SpringBootBasicsApplication`**: Main entry point for the Spring Boot application.
- **`com.example.basics.controller.HelloController`**: Simple "Hello World" REST endpoints (`/hello`, `/greet`).
- **`com.example.basics.controller.StudentRestController`**: REST API endpoints for managing students (`/api/students`).
- **`com.example.basics.service.StudentService`**: Business logic layer.
- **`com.example.basics.repository.StudentRepository`**: Data layer with in-memory storage.
- **`com.example.basics.model.Student`**: Student POJO model.

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.x

### Run Command
```bash
mvn spring-boot:run
```

### Endpoints to Test
- `GET http://localhost:8080/hello`
- `GET http://localhost:8080/greet?name=Khush`
- `GET http://localhost:8080/api/students`
- `GET http://localhost:8080/api/students/1`
