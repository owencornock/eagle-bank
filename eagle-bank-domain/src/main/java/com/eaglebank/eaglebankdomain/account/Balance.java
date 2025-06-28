package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;

import java.math.BigDecimal;
import java.util.Objects;

public record Balance(BigDecimal value) {
    public Balance {
        Objects.requireNonNull(value, "Balance cannot be null");
        if (value.signum() < 0) {
            throw new InvalidUserDataException("Balance cannot be negative");
        }
        // Normalize to 2 decimal places
        value = value.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }
}