package com.eaglebank.eaglebankapp.security;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import com.eaglebank.eaglebankdomain.user.*;
import com.eaglebank.eaglebanklogic.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenProvider jwt;

    @InjectMocks
    private AuthService authService;

    private PhoneNumber phone;
    private Address address;

    @BeforeEach
    void setUp() {
        phone = new PhoneNumber("+447911123456");
        address = new Address(
            "123 High Street",
            "London",
            "Greater London",
            "SW1A 1AA"
        );
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        String email = "user@example.com";
        String password = "password123";
        String hashedPassword = "hashed_password";
        UUID userId = UUID.randomUUID();
        String expectedToken = "jwt.token.here";

        User user = User.create(
                new FirstName("John"),
                new LastName("Doe"),
                new DateOfBirth(LocalDate.now().minusYears(25)),
                new EmailAddress(email),
                phone,
                address,
                new PasswordHash(hashedPassword)
        );
        // Use reflection to set the ID since it's normally generated
        setUserId(user, userId);

        when(userService.findByEmail(new EmailAddress(email))).thenReturn(Optional.of(user));
        when(encoder.matches(password, hashedPassword)).thenReturn(true);
        when(jwt.createToken(userId.toString())).thenReturn(expectedToken);

        // Act
        String token = authService.login(email, password);

        // Assert
        assertEquals(expectedToken, token);
        verify(userService).findByEmail(new EmailAddress(email));
        verify(encoder).matches(password, hashedPassword);
        verify(jwt).createToken(userId.toString());
    }

    @Test
    void shouldThrowBadCredentialsWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "user@example.com";
        String password = "wrong_password";
        String hashedPassword = "hashed_password";

        User user = User.create(
                new FirstName("John"),
                new LastName("Doe"),
                new DateOfBirth(LocalDate.now().minusYears(25)),
                new EmailAddress(email),
                phone,
                address,
                new PasswordHash(hashedPassword)
        );

        when(userService.findByEmail(new EmailAddress(email))).thenReturn(Optional.of(user));
        when(encoder.matches(password, hashedPassword)).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(email, password)
        );
        assertEquals("Invalid login", exception.getMessage());

        verify(userService).findByEmail(new EmailAddress(email));
        verify(encoder).matches(password, hashedPassword);
        verifyNoInteractions(jwt);
    }

    @Test
    void shouldThrowBadCredentialsWhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userService.findByEmail(new EmailAddress(email))).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(email, password)
        );
        assertEquals("Invalid login", exception.getMessage());

        verify(userService).findByEmail(new EmailAddress(email));
        verifyNoInteractions(encoder);
        verifyNoInteractions(jwt);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange
        String invalidEmail = "not-an-email";
        String password = "password123";

        // Act & Assert
        assertThrows(InvalidUserDataException.class,
                () -> authService.login(invalidEmail, password)
        );

        verifyNoInteractions(userService);
        verifyNoInteractions(encoder);
        verifyNoInteractions(jwt);
    }

    // Helper method to set the User ID using reflection
    private void setUserId(User user, UUID id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, new UserId(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }
    }
}