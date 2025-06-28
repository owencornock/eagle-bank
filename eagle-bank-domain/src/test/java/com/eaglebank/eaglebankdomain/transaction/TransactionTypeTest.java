package com.eaglebank.eaglebankdomain.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTypeTest {

    @Test
    void shouldHaveCorrectValues() {
        assertEquals(2, TransactionType.values().length,
                "Should have exactly two transaction types");
        assertTrue(hasTransactionType("DEPOSIT"),
                "Should have DEPOSIT type");
        assertTrue(hasTransactionType("WITHDRAWAL"),
                "Should have WITHDRAWAL type");
    }

    @Test
    void shouldConvertToAndFromString() {
        assertEquals("DEPOSIT", TransactionType.DEPOSIT.name(),
                "DEPOSIT should convert to string correctly");
        assertEquals("WITHDRAWAL", TransactionType.WITHDRAWAL.name(),
                "WITHDRAWAL should convert to string correctly");

        assertEquals(TransactionType.DEPOSIT,
                TransactionType.valueOf("DEPOSIT"),
                "Should parse DEPOSIT string correctly");
        assertEquals(TransactionType.WITHDRAWAL,
                TransactionType.valueOf("WITHDRAWAL"),
                "Should parse WITHDRAWAL string correctly");
    }

    @Test
    void shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionType.valueOf("INVALID_TYPE");
        }, "Should throw exception for invalid transaction type");
    }

    @Test
    void shouldBeComparable() {
        assertNotEquals(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL,
                "DEPOSIT should not equal WITHDRAWAL");
        assertNotEquals(TransactionType.WITHDRAWAL, TransactionType.DEPOSIT,
                "WITHDRAWAL should not equal DEPOSIT");
    }

    private boolean hasTransactionType(String typeName) {
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}