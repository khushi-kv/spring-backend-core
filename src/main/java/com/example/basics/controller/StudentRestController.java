package com.example.basics.controller;

import com.example.basics.dto.StudentRequestDto;
import com.example.basics.dto.StudentResponseDto;
import com.example.basics.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Student Controller", description = "REST APIs for Student CRUD Operations")
public class StudentRestController {

    private final StudentService studentService;

    @Autowired
    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Get All Students", description = "Retrieves a complete list of registered students.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved students list")
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Student by ID", description = "Fetches a single student entity by unique database ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found with provided ID")
    })
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @Operation(summary = "Create Student", description = "Registers a new student after validating input fields.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload / Validation error")
    })
    public ResponseEntity<StudentResponseDto> addStudent(@Valid @RequestBody StudentRequestDto dto) {
        StudentResponseDto createdStudent = studentService.addStudent(dto);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Student", description = "Updates an existing student by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student updated successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found with provided ID"),
        @ApiResponse(responseCode = "400", description = "Validation error in request body")
    })
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDto dto) {
        StudentResponseDto updatedStudent = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Student", description = "Deletes a student record by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found with provided ID")
    })
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
