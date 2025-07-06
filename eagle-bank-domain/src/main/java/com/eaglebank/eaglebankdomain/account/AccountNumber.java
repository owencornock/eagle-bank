package com.eaglebank.eaglebankdomain.account;

import java.util.Objects;

public record AccountNumber(String value) {
    public AccountNumber(String value) {
        this.value = Objects.requireNonNull(value, "Account number cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
    }

}