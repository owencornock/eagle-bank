package com.eaglebank.eaglebankdomain.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountNumberTest {

    @Test
    void shouldCreateValidAccountNumber() {
        AccountNumber number = new AccountNumber("12345678");
        assertEquals("12345678", number.value());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new AccountNumber(null);
        }, "Account number cannot be null");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenValueIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AccountNumber("");
        }, "Account number cannot be empty");

        assertThrows(IllegalArgumentException.class, () -> {
            new AccountNumber("   ");
        }, "Account number cannot be empty");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        AccountNumber number1 = new AccountNumber("12345678");
        AccountNumber number2 = new AccountNumber("12345678");
        AccountNumber number3 = new AccountNumber("87654321");

        assertEquals(number1, number2, "Same account numbers should be equal");
        assertNotEquals(number1, number3, "Different account numbers should not be equal");
        assertNotEquals(null, number1, "Account number should not be equal to null");
        assertNotEquals("12345678", number1, "Account number should not be equal to String");

        assertEquals(number1.hashCode(), number2.hashCode(),
                "Equal account numbers should have same hash code");
    }
}