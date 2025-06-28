package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void shouldGiveValidEmailAddress() {
        EmailAddress email = new EmailAddress("alice@example.com");
        assertEquals("alice@example.com", email.value());
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenEmailMissingAtSymbol() {
        assertThrows(InvalidUserDataException.class, () -> {
            new EmailAddress("aliceexample.com");
        });
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenEmailMissingLocalPart() {
        assertThrows(InvalidUserDataException.class, () -> {
            new EmailAddress("@example.com");
        });
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenEmailMissingDomain() {
        assertThrows(InvalidUserDataException.class, () -> {
            new EmailAddress("alice@");
        });
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenEmailHasSpaces() {
        assertThrows(InvalidUserDataException.class, () -> {
            new EmailAddress("alice @example.com");
        });
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new EmailAddress(null);
        });
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Arrange
        EmailAddress email1 = new EmailAddress("test@example.com");
        EmailAddress email2 = new EmailAddress("test@example.com");
        EmailAddress email3 = new EmailAddress("different@example.com");

        // Test equals
        assertEquals(email1, email1, "Email should be equal to itself");
        assertEquals(email1, email2, "Email addresses with same value should be equal");
        assertNotEquals(email1, email3, "Different email addresses should not be equal");
        assertNotEquals(null, email1, "Email should not be equal to null");
        assertNotEquals("test@example.com", email1, "Email should not be equal to String");

        // Test hashCode
        assertEquals(email1.hashCode(), email2.hashCode(),
                "Equal email addresses should have same hash code");
        assertNotEquals(email1.hashCode(), email3.hashCode(),
                "Different email addresses should have different hash codes");
    }

    @Test
    void shouldHandleCaseSensitivity() {
        // Arrange
        EmailAddress email1 = new EmailAddress("Test@Example.com");
        EmailAddress email2 = new EmailAddress("test@example.com");

        // Assert - assuming case-sensitive comparison as per implementation
        assertNotEquals(email1, email2, "Email addresses with different case should not be equal");
        assertNotEquals(email1.hashCode(), email2.hashCode(),
                "Email addresses with different case should have different hash codes");
    }

    @Test
    void shouldHandleLeadingAndTrailingSpaces() {
        // Arrange & Act & Assert
        assertThrows(InvalidUserDataException.class,
                () -> new EmailAddress(" test@example.com"),
                "Email with leading space should be invalid");

        assertThrows(InvalidUserDataException.class,
                () -> new EmailAddress("test@example.com "),
                "Email with trailing space should be invalid");
    }

    @Test
    void shouldHandleDifferentClassTypes() {
        // Arrange
        EmailAddress email = new EmailAddress("test@example.com");
        Object differentClass = new Object(); // Using plain Object class is sufficient

        // Assert
        assertNotEquals(email, differentClass, "Email should not be equal to different class type");
    }

    @Test
    void shouldHandleNullValue() {
        // Arrange
        EmailAddress email = new EmailAddress("test@example.com");

        // Assert
        assertNotEquals(null, email, "Email should not be equal to null");
    }
}