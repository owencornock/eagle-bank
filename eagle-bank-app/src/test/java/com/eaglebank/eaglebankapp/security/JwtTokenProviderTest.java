package com.eaglebank.eaglebankapp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
    }

    @Test
    void shouldCreateAndParseTokenSuccessfully() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        // Act
        String token = tokenProvider.createToken(userId);
        String extractedUserId = tokenProvider.getUserId(token);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void shouldRejectInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThatThrownBy(() -> tokenProvider.getUserId(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreateTokenWithCorrectFormat() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        // Act
        String token = tokenProvider.createToken(userId);

        // Assert
        assertThat(token).contains(".");  // JWT format contains at least two dots
        assertThat(token.split("\\.")).hasSize(3);  // Header, payload, and signature
    }
}