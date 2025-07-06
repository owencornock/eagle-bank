package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    void shouldCreateValidPhoneNumber() {
        PhoneNumber phone = new PhoneNumber("+447911123456");
        assertEquals("+447911123456", phone.value());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new PhoneNumber(null),
            "Phone number cannot be null"
        );
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenEmpty() {
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber(""),
            "Phone number cannot be empty"
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("   "),
            "Phone number cannot be empty"
        );
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenFormatIsInvalid() {
        // No plus prefix
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("447911123456")
        );

        // Zero after plus
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("+0123456789")
        );

        // Too short
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("+1")
        );

        // Too long
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("+123456789012345678")
        );

        // Invalid characters
        assertThrows(InvalidUserDataException.class, () ->
            new PhoneNumber("+44abc123456")
        );
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        PhoneNumber phone1 = new PhoneNumber("+447911123456");
        PhoneNumber phone2 = new PhoneNumber("+447911123456");
        PhoneNumber phone3 = new PhoneNumber("+447911123457");

        assertEquals(phone1, phone2, "Same phone numbers should be equal");
        assertNotEquals(phone1, phone3, "Different phone numbers should not be equal");
        assertNotEquals(phone1, null, "Phone number should not be equal to null");
        assertNotEquals("+447911123456", phone1, "Phone number should not be equal to String");

        assertEquals(phone1.hashCode(), phone2.hashCode(),
                "Equal phone numbers should have same hash code");
    }
}