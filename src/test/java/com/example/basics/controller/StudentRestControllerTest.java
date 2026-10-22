package com.example.basics.controller;

import com.example.basics.config.SecurityConfig;
import com.example.basics.dto.StudentRequestDto;
import com.example.basics.dto.StudentResponseDto;
import com.example.basics.security.JwtAuthenticationEntryPoint;
import com.example.basics.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentRestController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@WithMockUser
class StudentRestControllerTest extends SecuredWebMvcTest {

    private static final String VALID_STUDENT = """
            {"name": "Asha", "course": "Java"}
            """;

    private final StudentResponseDto asha = new StudentResponseDto(1L, "Asha", "Java");

    @MockitoBean
    private StudentService studentService;

    @Test
    void getAllStudentsReturnsList() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(asha));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Asha"));
    }

    @Test
    void getStudentByIdReturnsStudent() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(asha);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course").value("Java"));
    }

    @Test
    void addStudentReturns201() throws Exception {
        when(studentService.addStudent(any(StudentRequestDto.class))).thenReturn(asha);

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(VALID_STUDENT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addStudentWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"course\": \"Java\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStudentReturns200() throws Exception {
        when(studentService.updateStudent(eq(1L), any(StudentRequestDto.class))).thenReturn(asha);

        mockMvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON).content(VALID_STUDENT))
                .andExpect(status().isOk());
    }

    @Test
    void deleteStudentReturns204() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());
        verify(studentService).deleteStudent(1L);
    }
}
