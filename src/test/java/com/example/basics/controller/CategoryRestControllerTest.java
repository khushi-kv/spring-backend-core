package com.example.basics.controller;

import com.example.basics.config.SecurityConfig;
import com.example.basics.dto.CategoryRequestDto;
import com.example.basics.dto.CategoryResponseDto;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.security.JwtAuthenticationEntryPoint;
import com.example.basics.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryRestController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class CategoryRestControllerTest extends SecuredWebMvcTest {

    private static final String VALID_CATEGORY = """
            {"name": "Electronics", "description": "Gadgets"}
            """;

    private final CategoryResponseDto electronics = new CategoryResponseDto(1L, "Electronics", "Gadgets", 2);

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @WithMockUser(roles = "STAFF")
    void createCategoryAsStaffReturns201() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(electronics);

        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(VALID_CATEGORY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategoryAsPlainUserReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(VALID_CATEGORY))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser
    void getAllCategoriesReturnsList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(electronics));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemCount").value(2));
    }

    @Test
    @WithMockUser
    void getCategoryByIdWithUnknownIdReturns404() throws Exception {
        when(categoryService.getCategoryById(9L)).thenThrow(new ResourceNotFoundException("Category not found with ID: 9"));

        mockMvc.perform(get("/api/v1/categories/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getCategoryByIdReturnsCategory() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(electronics);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Gadgets"));
    }
}
