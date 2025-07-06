package com.eaglebank.eaglebankdomain.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTypeTest {

    @Test
    void shouldHaveCorrectValues() {
        assertEquals(3, AccountType.values().length, 
                "Should have exactly three account types");
        assertNotNull(AccountType.valueOf("CHECKING"));
        assertNotNull(AccountType.valueOf("SAVINGS"));
        assertNotNull(AccountType.valueOf("BUSINESS"));
    }

    @Test
    void shouldBeComparable() {
        AccountType checking = AccountType.CHECKING;
        AccountType savings = AccountType.SAVINGS;
        AccountType business = AccountType.BUSINESS;

        assertNotEquals(checking, savings);
        assertNotEquals(savings, business);
        assertNotEquals(checking, business);
    }

    @Test
    void shouldConvertToAndFromString() {
        assertEquals("CHECKING", AccountType.CHECKING.toString());
        assertEquals("SAVINGS", AccountType.SAVINGS.toString());
        assertEquals("BUSINESS", AccountType.BUSINESS.toString());
        
        assertEquals(AccountType.CHECKING, AccountType.valueOf("CHECKING"));
        assertEquals(AccountType.SAVINGS, AccountType.valueOf("SAVINGS"));
        assertEquals(AccountType.BUSINESS, AccountType.valueOf("BUSINESS"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            AccountType.valueOf("INVALID");
        }, "Should throw exception for invalid account type");
    }
}