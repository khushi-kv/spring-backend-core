package com.example.basics.controller;

import com.example.basics.config.SecurityConfig;
import com.example.basics.dto.AuthRequestDto;
import com.example.basics.dto.AuthResponseDto;
import com.example.basics.dto.RegisterRequestDto;
import com.example.basics.exception.DuplicateResourceException;
import com.example.basics.security.JwtAuthenticationEntryPoint;
import com.example.basics.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The auth endpoints are public, so these tests run without {@code @WithMockUser}.
 */
@WebMvcTest(AuthRestController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class AuthRestControllerTest extends SecuredWebMvcTest {

    private static final String REGISTER_BODY = """
            {"username": "khushi", "email": "khushi@example.com", "password": "secret123"}
            """;
    private static final String LOGIN_BODY = """
            {"usernameOrEmail": "khushi", "password": "secret123"}
            """;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerWithoutTokenReturns201WithJwt() throws Exception {
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(
                new AuthResponseDto("User registered successfully!", "jwt-token", "khushi", Set.of("ROLE_USER")));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void registerWithTakenUsernameReturns409() throws Exception {
        when(authService.register(any(RegisterRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Username 'khushi' is already taken."));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void registerWithInvalidEmailAndShortPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"khushi\", \"email\": \"not-an-email\", \"password\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(2));
    }

    @Test
    void loginReturns200WithJwt() throws Exception {
        when(authService.login(any(AuthRequestDto.class))).thenReturn(
                new AuthResponseDto("User logged in successfully!", "jwt-token", "khushi", Set.of("ROLE_USER")));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("khushi"));
    }

    @Test
    void loginWithWrongPasswordReturns401Json() throws Exception {
        when(authService.login(any(AuthRequestDto.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password."));
    }
}
