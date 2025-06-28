package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LastNameTest {

    @Test
    void shouldGiveValidLastName() {
        LastName ln = new LastName("Smith");
        assertEquals("Smith", ln.getValue());
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWithNoValueProvided() {
        assertThrows(InvalidUserDataException.class, () -> {
            new LastName("");
        });
        assertThrows(InvalidUserDataException.class, () -> {
            new LastName("   ");
        });
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenValueIsTooLong() {
        String longName = "A".repeat(51);
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new LastName(longName);
        });
        assertTrue(ex.getMessage().contains("LastName must be 1–50 characters"));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new LastName(null);
        });
    }
}
