package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;

import java.util.Objects;

public record AccountName(String value) {
    public AccountName {
        Objects.requireNonNull(value, "AccountName cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new InvalidUserDataException("AccountName must be 1–100 characters");
        }
        value = trimmed;
    }
}