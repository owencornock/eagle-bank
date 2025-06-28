package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountNameTest {

    @Test
    void shouldCreateValidAccountName() {
        AccountName name = new AccountName("Savings Account");
        assertEquals("Savings Account", name.value());
    }

    @Test
    void shouldTrimWhitespace() {
        AccountName name = new AccountName("  Checking Account  ");
        assertEquals("Checking Account", name.value(),
                "AccountName should trim leading and trailing whitespace");
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWithEmptyValue() {
        assertThrows(InvalidUserDataException.class, () -> {
            new AccountName("");
        }, "Empty account name should throw InvalidUserDataException");

        assertThrows(InvalidUserDataException.class, () -> {
            new AccountName("   ");
        }, "Whitespace-only account name should throw InvalidUserDataException");
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenValueIsTooLong() {
        String longName = "A".repeat(101);
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new AccountName(longName);
        });
        assertTrue(ex.getMessage().contains("AccountName must be 1–100 characters"),
                "Exception message should mention the length constraint");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new AccountName(null);
        }, "Null account name should throw NullPointerException");
    }

    @Test
    void shouldAcceptMaximumLengthName() {
        String maxLengthName = "A".repeat(100);
        AccountName name = new AccountName(maxLengthName);
        assertEquals(maxLengthName, name.value(),
                "Should accept account name of maximum allowed length (100 characters)");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        AccountName name1 = new AccountName("Savings Account");
        AccountName name2 = new AccountName("Savings Account");
        AccountName name3 = new AccountName("Checking Account");

        assertEquals(name1, name2, "Same account names should be equal");
        assertNotEquals(name1, name3, "Different account names should not be equal");
        assertNotEquals(null, name1, "Account name should not be equal to null");
        assertNotEquals("Savings Account", name1, "Account name should not be equal to String");

        assertEquals(name1.hashCode(), name2.hashCode(),
                "Equal account names should have same hash code");
    }
}