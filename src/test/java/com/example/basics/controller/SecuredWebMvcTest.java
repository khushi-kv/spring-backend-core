package com.example.basics.controller;

import com.example.basics.security.CustomUserDetailsService;
import com.example.basics.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared setup for controller slice tests. Subclasses add
 * {@code @WebMvcTest(XController.class)} and {@code @Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})}
 * so the real security rules (401 / 403) and the GlobalExceptionHandler are applied.
 * The JWT beans are mocked; tests authenticate with {@code @WithMockUser} instead of real tokens.
 */
abstract class SecuredWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;
}
