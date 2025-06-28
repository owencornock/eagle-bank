package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankapp.security.AuthService;
import com.eaglebank.eaglebankdomain.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        String email = "user@example.com";
        String password = "password123";
        String expectedToken = "jwt.token.here";

        AuthController.LoginRequest request = new AuthController.LoginRequest(email, password);
        when(authService.login(email, password)).thenReturn(expectedToken);

        // Act
        ResponseEntity<AuthController.LoginResponse> response = controller.login(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo(expectedToken);

        verify(authService).login(email, password);
    }

    @Test
    void shouldHandleFailedAuthentication() {
        String email = "user@example.com";
        String password = "wrongpassword";
        AuthController.LoginRequest request = new AuthController.LoginRequest(email, password);

        when(authService.login(anyString(), anyString()))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        assertThat(
                catchThrowable(() -> controller.login(request))
        ).isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(authService).login(email, password);
    }

    @Test
    void shouldHandleNullCredentials() {
        AuthController.LoginRequest request = new AuthController.LoginRequest(null, null);

        when(authService.login(null, null))
                .thenThrow(new AuthenticationException("Email and password are required"));

        assertThat(
                catchThrowable(() -> controller.login(request))
        ).isInstanceOf(AuthenticationException.class)
                .hasMessage("Email and password are required");

        verify(authService).login(null, null);
    }

    @Test
    void shouldHandleEmptyCredentials() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("", "");

        when(authService.login("", ""))
                .thenThrow(new AuthenticationException("Email and password cannot be empty"));
        assertThat(
                catchThrowable(() -> controller.login(request))
        ).isInstanceOf(AuthenticationException.class)
                .hasMessage("Email and password cannot be empty");

        verify(authService).login("", "");
    }

    private Throwable catchThrowable(ThrowingCallable callable) {
        try {
            callable.call();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    @FunctionalInterface
    interface ThrowingCallable {
        void call() throws Throwable;
    }
}