package com.example.basics.security;

import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameMapsRolesToAuthorities() {
        User user = new User("staff", "staff@inventory.com", "hash", Set.of(Role.ROLE_STAFF));
        when(userRepository.findByUsernameOrEmail("staff@inventory.com", "staff@inventory.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("staff@inventory.com");

        assertThat(details.getUsername()).isEqualTo("staff");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_STAFF");
    }

    @Test
    void loadUserByUsernameWithUnknownUserThrows() {
        when(userRepository.findByUsernameOrEmail("ghost", "ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
