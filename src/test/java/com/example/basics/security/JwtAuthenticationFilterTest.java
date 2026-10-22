package com.example.basics.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenAuthenticatesTheUser() throws Exception {
        request.addHeader("Authorization", "Bearer good-token");
        when(jwtTokenProvider.validateToken("good-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("good-token")).thenReturn("admin");
        when(customUserDetailsService.loadUserByUsername("admin")).thenReturn(
                new User("admin", "hash", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("admin");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void invalidTokenLeavesRequestUnauthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void missingHeaderSkipsTokenValidation() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).validateToken(anyString());
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void nonBearerHeaderSkipsTokenValidation() throws Exception {
        request.addHeader("Authorization", "Basic YWRtaW46cHc=");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }
}
