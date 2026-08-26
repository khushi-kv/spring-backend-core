package com.example.basics.controller;

import com.example.basics.dto.StudentRequestDto;
import com.example.basics.dto.StudentResponseDto;
import com.example.basics.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Student management.
 * Base URL: http://localhost:8080/api/students
 */
@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService studentService;

    @Autowired
    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET all students -> GET http://localhost:8080/api/students
    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    // GET student by ID -> GET http://localhost:8080/api/students/1
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    // POST create student -> POST http://localhost:8080/api/students
    @PostMapping
    public ResponseEntity<StudentResponseDto> addStudent(@Valid @RequestBody StudentRequestDto dto) {
        StudentResponseDto createdStudent = studentService.addStudent(dto);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    // PUT update student -> PUT http://localhost:8080/api/students/1
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDto dto) {
        StudentResponseDto updatedStudent = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(updatedStudent);
    }

    // DELETE student -> DELETE http://localhost:8080/api/students/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
