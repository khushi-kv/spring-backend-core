package com.example.basics.service;

import com.example.basics.dto.StudentRequestDto;
import com.example.basics.dto.StudentResponseDto;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Student;
import com.example.basics.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Service marks this class as a Service layer component holding business logic.
 * Encapsulates CRUD operations, DTO mapping, and domain validation.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToResponseDto(student);
    }

    public StudentResponseDto addStudent(StudentRequestDto dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setCourse(dto.getCourse());

        Student savedStudent = studentRepository.save(student);
        return mapToResponseDto(savedStudent);
    }

    public StudentResponseDto updateStudent(Long id, StudentRequestDto dto) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update. Student not found with ID: " + id));

        existingStudent.setName(dto.getName());
        existingStudent.setCourse(dto.getCourse());

        Student updatedStudent = studentRepository.save(existingStudent);
        return mapToResponseDto(updatedStudent);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Student not found with ID: " + id);
        }
        studentRepository.deleteById(id);
    }

    // Helper method to convert Entity -> Response DTO
    private StudentResponseDto mapToResponseDto(Student student) {
        return new StudentResponseDto(student.getId(), student.getName(), student.getCourse());
    }
}
