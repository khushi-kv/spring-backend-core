package com.example.basics.repository;

import com.example.basics.model.Student;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @Repository marks this class as a Data Access Object (DAO).
 * Spring manages this class as a Spring Bean in the IoC Container.
 * It also converts database-specific exceptions into Spring's DataAccessException hierarchy.
 */
@Repository
public class StudentRepository {

    // In-memory data store for demonstration
    private final List<Student> students = new ArrayList<>();

    public StudentRepository() {
        // Pre-populating dummy data
        students.add(new Student(1L, "Alice", "Core Java & Spring Boot"));
        students.add(new Student(2L, "Bob", "Full Stack Development"));
        students.add(new Student(3L, "Charlie", "Data Structures & Algorithms"));
    }

    public List<Student> findAll() {
        return students;
    }

    public Student findById(Long id) {
        return students.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Student save(Student student) {
        students.add(student);
        return student;
    }
}
