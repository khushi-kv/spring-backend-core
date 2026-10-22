package com.example.basics.service;

import com.example.basics.dto.AuthRequestDto;
import com.example.basics.dto.AuthResponseDto;
import com.example.basics.dto.RegisterRequestDto;
import com.example.basics.exception.DuplicateResourceException;
import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import com.example.basics.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private final RegisterRequestDto registerRequest = new RegisterRequestDto("khushi", "khushi@example.com", "secret123");

    @Test
    void registerSavesUserWithEncodedPasswordAndUserRoleThenReturnsToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("khushi", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.existsByUsername("khushi")).thenReturn(false);
        when(userRepository.existsByEmail("khushi@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponseDto response = authService.register(registerRequest);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPassword()).isEqualTo("hashed");
        assertThat(savedUser.getValue().getRoles()).containsExactly(Role.ROLE_USER);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("khushi");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void registerWithTakenUsernameThrowsConflict() {
        when(userRepository.existsByUsername("khushi")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'khushi'");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerWithTakenEmailThrowsConflict() {
        when(userRepository.existsByUsername("khushi")).thenReturn(false);
        when(userRepository.existsByEmail("khushi@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'khushi@example.com'");
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenAndRolesOfAuthenticatedUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponseDto response = authService.login(new AuthRequestDto("admin", "pw"));

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void loginWithWrongPasswordPropagatesAuthenticationFailure() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        AuthRequestDto request = new AuthRequestDto("admin", "wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
