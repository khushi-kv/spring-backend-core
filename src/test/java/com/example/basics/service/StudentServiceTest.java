package com.example.basics.service;

import com.example.basics.dto.StudentRequestDto;
import com.example.basics.dto.StudentResponseDto;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Student;
import com.example.basics.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getAllStudentsMapsEveryStudent() {
        when(studentRepository.findAll()).thenReturn(List.of(
                new Student(1L, "Asha", "Java"),
                new Student(2L, "Ravi", "Spring")));

        List<StudentResponseDto> students = studentService.getAllStudents();

        assertThat(students).extracting(StudentResponseDto::getName).containsExactly("Asha", "Ravi");
    }

    @Test
    void getStudentByIdReturnsStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(new Student(1L, "Asha", "Java")));

        StudentResponseDto student = studentService.getStudentById(1L);

        assertThat(student.getId()).isEqualTo(1L);
        assertThat(student.getCourse()).isEqualTo("Java");
    }

    @Test
    void getStudentByIdWithUnknownIdThrowsNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student not found with ID: 1");
    }

    @Test
    void addStudentSavesAndReturnsStudent() {
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        StudentResponseDto student = studentService.addStudent(new StudentRequestDto("Meera", "Docker"));

        assertThat(student.getId()).isEqualTo(3L);
        assertThat(student.getName()).isEqualTo("Meera");
    }

    @Test
    void updateStudentChangesNameAndCourse() {
        Student existing = new Student(1L, "Asha", "Java");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(existing)).thenReturn(existing);

        StudentResponseDto student = studentService.updateStudent(1L, new StudentRequestDto("Asha K", "Kotlin"));

        assertThat(student.getName()).isEqualTo("Asha K");
        assertThat(student.getCourse()).isEqualTo("Kotlin");
    }

    @Test
    void updateStudentWithUnknownIdThrowsNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        StudentRequestDto request = new StudentRequestDto("Asha", "Java");

        assertThatThrownBy(() -> studentService.updateStudent(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith("Cannot update.");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void deleteStudentDeletesExistingStudent() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        studentService.deleteStudent(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    void deleteStudentWithUnknownIdThrowsNotFound() {
        when(studentRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.deleteStudent(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageStartingWith("Cannot delete.");
        verify(studentRepository, never()).deleteById(any());
    }
}
