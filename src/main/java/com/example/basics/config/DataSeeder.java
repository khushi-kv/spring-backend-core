package com.example.basics.config;

import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Seeds default ADMIN and STAFF users on application startup (dev profile).
 * Uses the actual BCrypt PasswordEncoder bean to ensure password hashes are correct.
 */
@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed ADMIN user (password: admin123)
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@inventory.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(Role.ROLE_ADMIN));
                userRepository.save(admin);
                logger.info("✅ Seeded ADMIN user: admin / admin123");
            }

            // Seed STAFF user (password: staff123)
            if (!userRepository.existsByUsername("staff")) {
                User staff = new User();
                staff.setUsername("staff");
                staff.setEmail("staff@inventory.com");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRoles(Set.of(Role.ROLE_STAFF));
                userRepository.save(staff);
                logger.info("✅ Seeded STAFF user: staff / staff123");
            }
        };
    }
}
