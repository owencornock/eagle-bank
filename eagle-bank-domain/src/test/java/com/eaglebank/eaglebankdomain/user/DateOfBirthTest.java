package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateOfBirthTest {

    @Test
    void shouldStoreValidDateOfBirth() {
        LocalDate birthday = LocalDate.now().minusYears(20);
        DateOfBirth dob = new DateOfBirth(birthday);
        assertEquals(birthday, dob.getValue());
    }

    @Test
    void shouldAllowExact18YearsOld() {
        LocalDate exact18 = LocalDate.now().minusYears(18);
        DateOfBirth dob = new DateOfBirth(exact18);
        assertEquals(exact18, dob.getValue());
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenValueIsNull() {
        assertThrows(InvalidUserDataException.class, () -> new DateOfBirth(null));
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenUserIsTooYoung() {
        LocalDate tooYoung = LocalDate.now().minusYears(17);
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () ->
                new DateOfBirth(tooYoung)
        );
        assertTrue(ex.getMessage().contains("User must be at least 18 years old"));
    }
}
