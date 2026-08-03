package com.example.basics.service;

import com.example.basics.model.Student;
import com.example.basics.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Service marks this class as a Service layer component holding business logic.
 * Spring automatically registers this as a Spring Bean in the IoC container.
 */
@Service
public class StudentService {

    // Dependency on StudentRepository
    private final StudentRepository studentRepository;

    /**
     * Dependency Injection (DI) via Constructor Injection (RECOMMENDED BEST PRACTICE).
     * Spring automatically injects the StudentRepository Bean when creating StudentService.
     * Note: @Autowired is optional on single constructors in Spring 4.3+, but included here for clarity.
     */
    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        // Business logic can go here (e.g. validation, filtering)
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student addStudent(Student student) {
        if (student.getName() == null || student.getName().isBlank()) {
            throw new IllegalArgumentException("Student name cannot be empty!");
        }
        return studentRepository.save(student);
    }
}
