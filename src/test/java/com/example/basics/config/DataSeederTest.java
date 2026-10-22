package com.example.basics.config;

import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final DataSeeder dataSeeder = new DataSeeder();

    @Test
    void seedsAdminAndStaffWithEncodedPasswords() throws Exception {
        configurePasswords("admin-pw", "staff-pw");
        when(passwordEncoder.encode("admin-pw")).thenReturn("admin-hash");
        when(passwordEncoder.encode("staff-pw")).thenReturn("staff-hash");

        dataSeeder.seedUsers(userRepository, passwordEncoder).run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(User::getUsername).containsExactly("admin", "staff");
        assertThat(saved.getAllValues()).extracting(User::getPassword).containsExactly("admin-hash", "staff-hash");
        assertThat(saved.getAllValues().get(0).getRoles()).containsExactly(Role.ROLE_ADMIN);
        assertThat(saved.getAllValues().get(1).getRoles()).containsExactly(Role.ROLE_STAFF);
    }

    @Test
    void skipsUsersThatAlreadyExist() throws Exception {
        configurePasswords("admin-pw", "staff-pw");
        when(userRepository.existsByUsername(any())).thenReturn(true);

        dataSeeder.seedUsers(userRepository, passwordEncoder).run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void skipsUsersWithoutConfiguredPassword() throws Exception {
        configurePasswords("", null);

        dataSeeder.seedUsers(userRepository, passwordEncoder).run();

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    private void configurePasswords(String adminPassword, String staffPassword) {
        ReflectionTestUtils.setField(dataSeeder, "adminPassword", adminPassword);
        ReflectionTestUtils.setField(dataSeeder, "staffPassword", staffPassword);
    }
}
