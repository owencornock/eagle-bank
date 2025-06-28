package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountTest {

    @Test
    void shouldCreateValidAmountWithZero() {
        Amount amount = new Amount(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, amount.value(),
                "Should accept zero amount");
    }

    @Test
    void shouldCreateValidAmountWithPositiveValue() {
        Amount amount = new Amount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), amount.value(),
                "Should accept positive amount");
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWithNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-1.00");
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new Amount(negativeAmount);
        });
        assertTrue(ex.getMessage().contains("Amount must be non-negative"),
                "Exception message should mention that negative amount is not allowed");
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenValueIsNull() {
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new Amount(null);
        });
        assertTrue(ex.getMessage().contains("Amount must be non-negative"),
                "Exception message should mention that amount must be non-negative");
    }

    @Test
    void shouldHandleLargeAmounts() {
        BigDecimal largeAmount = new BigDecimal("999999999999.99");
        Amount amount = new Amount(largeAmount);
        assertEquals(largeAmount, amount.value(),
                "Should handle large amount values");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Amount amount1 = new Amount(new BigDecimal("100.00"));
        Amount amount2 = new Amount(new BigDecimal("100.00"));
        Amount amount3 = new Amount(new BigDecimal("200.00"));

        // Test equals
        assertEquals(amount1, amount2, "Same amounts should be equal");
        assertNotEquals(amount1, amount3, "Different amounts should not be equal");
        assertNotEquals(amount1, null, "Amount should not be equal to null");
        assertNotEquals(amount1, new BigDecimal("100.00"),
                "Amount should not be equal to BigDecimal");

        // Test hashCode
        assertEquals(amount1.hashCode(), amount2.hashCode(),
                "Equal amounts should have same hash code");
    }

    @Test
    void shouldAcceptVerySmallPositiveAmounts() {
        Amount amount = new Amount(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("0.01"), amount.value(),
                "Should accept small positive amounts");
    }
}