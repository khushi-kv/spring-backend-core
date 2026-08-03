package com.example.basics.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.basics.model.Student;
import com.example.basics.service.StudentService;

/**
 * Step 3: REST API Controller for Student management. Base path:
 * http://localhost:8080/api/students
 */
@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService studentService;

    // Spring injects StudentService automatically
    @Autowired
    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET all students -> http://localhost:8080/api/students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    // GET student by ID -> http://localhost:8080/api/students/1
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    // POST create student -> http://localhost:8080/api/students
    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        return studentService.addStudent(student);
    }
}
