package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;

import java.math.BigDecimal;

public record Amount(BigDecimal value) {
    public Amount {
        if (value == null || value.signum()<0)
            throw new InvalidUserDataException("Amount must be non-negative");
    }
}