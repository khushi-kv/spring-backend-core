package com.example.basics.controller;

import com.example.basics.config.SecurityConfig;
import com.example.basics.security.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@WithMockUser
class HelloControllerTest extends SecuredWebMvcTest {

    @Test
    void helloReturnsWelcomeText() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to Spring Boot!"));
    }

    @Test
    void greetUsesDefaultNameWhenMissing() throws Exception {
        mockMvc.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Learner! You are ready to learn Spring Boot."));
    }

    @Test
    void greetUsesGivenName() throws Exception {
        mockMvc.perform(get("/greet").param("name", "Khushi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Khushi! You are ready to learn Spring Boot."));
    }
}
