package com.example.basics.config;

import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Seeds default ADMIN and STAFF users on application startup (dev profile only).
 * Passwords are read from the SEED_ADMIN_PASSWORD / SEED_STAFF_PASSWORD environment
 * variables so that no credentials live in source control; a user is skipped when
 * its password is not provided.
 * Uses the actual BCrypt PasswordEncoder bean to ensure password hashes are correct.
 */
@Configuration
@Profile("dev")
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.staff-password:}")
    private String staffPassword;

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(userRepository, passwordEncoder, "admin", "admin@inventory.com", adminPassword, Role.ROLE_ADMIN);
            seedUser(userRepository, passwordEncoder, "staff", "staff@inventory.com", staffPassword, Role.ROLE_STAFF);
        };
    }

    private void seedUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          String username, String email, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        if (!StringUtils.hasText(rawPassword)) {
            logger.warn("Skipping seed of '{}' user: no password configured (see app.seed.* in application-dev.properties)",
                    username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(role));
        userRepository.save(user);
        logger.info("Seeded {} user: {}", role, username);
    }
}
