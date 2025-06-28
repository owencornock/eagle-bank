package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {

    @Test
    void shouldCreateValidBalanceWithZero() {
        Balance balance = new Balance(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), balance.value(),
                "Should accept zero balance with standard scale of 2");
    }

    @Test
    void shouldCreateValidBalanceWithPositiveAmount() {
        Balance balance = new Balance(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), balance.value(),
                "Should accept positive balance");
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWithNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-1.00");
        InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () -> {
            new Balance(negativeAmount);
        });
        assertTrue(ex.getMessage().contains("Balance cannot be negative"),
                "Exception message should mention that negative balance is not allowed");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new Balance(null);
        }, "Null balance should throw NullPointerException");
    }

    @Test
    void shouldHandleLargeBalances() {
        BigDecimal largeAmount = new BigDecimal("999999999999.99");
        Balance balance = new Balance(largeAmount);
        assertEquals(largeAmount, balance.value(),
                "Should handle large balance values");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Balance balance1 = new Balance(new BigDecimal("100.00"));
        Balance balance2 = new Balance(new BigDecimal("100.00"));
        Balance balance3 = new Balance(new BigDecimal("200.00"));

        // Test equals
        assertEquals(balance1, balance2, "Same balance amounts should be equal");
        assertNotEquals(balance1, balance3, "Different balance amounts should not be equal");
        assertNotEquals(balance1, null, "Balance should not be equal to null");
        assertNotEquals(balance1, new BigDecimal("100.00"),
                "Balance should not be equal to BigDecimal");

        // Test hashCode
        assertEquals(balance1.hashCode(), balance2.hashCode(),
                "Equal balances should have same hash code");
    }

    @Test
    void shouldHandleScalePrecision() {
        Balance balance1 = new Balance(new BigDecimal("100.00"));
        Balance balance2 = new Balance(new BigDecimal("100.0"));

        assertEquals(balance1, balance2,
                "Balances with different scale but same value should be equal");
    }

    @Test
    void shouldAcceptVerySmallPositiveBalances() {
        Balance balance = new Balance(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("0.01"), balance.value(),
                "Should accept small positive balances");
    }
}