package com.example.basics.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_STAFF")));

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = newProvider(randomBase64Key(), 60_000);
    }

    @Test
    void generatedTokenIsValidAndCarriesUsernameAndRoles() {
        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("admin");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String foreignToken = newProvider(randomBase64Key(), 60_000).generateToken(authentication);

        assertThat(jwtTokenProvider.validateToken(foreignToken)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        String key = randomBase64Key();
        String expiredToken = newProvider(key, -1_000).generateToken(authentication);

        assertThat(newProvider(key, 60_000).validateToken(expiredToken)).isFalse();
    }

    @Test
    void unsignedTokenIsRejected() {
        String unsignedToken = Jwts.builder().subject("admin").compact();

        assertThat(jwtTokenProvider.validateToken(unsignedToken)).isFalse();
    }

    @Test
    void malformedTokenIsRejected() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void emptyTokenIsRejected() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void tamperedPayloadIsRejected() {
        String[] parts = jwtTokenProvider.generateToken(authentication).split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacker\",\"roles\":\"ROLE_ADMIN\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(jwtTokenProvider.validateToken(parts[0] + "." + forgedPayload + "." + parts[2])).isFalse();
    }

    private static JwtTokenProvider newProvider(String base64Secret, long expirationMs) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", base64Secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", expirationMs);
        return provider;
    }

    // A fresh 512-bit key per test run: no secret is stored anywhere in the repository
    private static String randomBase64Key() {
        byte[] key = new byte[64];
        RANDOM.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
