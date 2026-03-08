package com.manish.spring.security.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    // ─── passwordEncoder ──────────────────────────────────────────────────────

    @Test
    void passwordEncoder_returnsBCryptPasswordEncoder() {
        // Act
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Assert
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_encodesPasswordCorrectly() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "mySecretPassword";

        // Act
        String encodedPassword = encoder.encode(rawPassword);

        // Assert
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void passwordEncoder_doesNotMatchIncorrectPassword() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode("correctPassword");

        // Act & Assert
        assertThat(encoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    void passwordEncoder_producesDifferentHashesForSameInput() {
        // BCrypt uses a random salt, so two encodes of the same value differ
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "samePassword";

        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(raw, hash1)).isTrue();
        assertThat(encoder.matches(raw, hash2)).isTrue();
    }
}
