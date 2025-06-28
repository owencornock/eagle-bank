package com.eaglebank.eaglebankdomain.user;


import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException("Password hash cannot be blank");
        }
    }
}