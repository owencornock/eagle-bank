package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirstNameTest {

    @Test
    void shouldGiveValidFirstName() {
        FirstName fn = new FirstName("Alice");
        assertEquals("Alice", fn.getValue());
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWithNoValueProvided() {
        assertThrows(InvalidUserDataException.class, () -> {
            new FirstName("");
        });
        assertThrows(InvalidUserDataException.class, () -> {
            new FirstName("   ");
        });
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenValueIsTooLong() {
        String longName = "A".repeat(51);
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new FirstName(longName);
        });
        assertTrue(ex.getMessage().contains("FirstName must be 1–50 characters"));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new FirstName(null);
        });
    }
}
