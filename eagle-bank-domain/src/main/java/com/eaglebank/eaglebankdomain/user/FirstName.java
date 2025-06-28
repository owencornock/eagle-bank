package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class FirstName {
    @NonNull
    private final String value;

    public FirstName(String value) {
        if (value.isBlank() || value.length() > 50) {
            throw new InvalidUserDataException("FirstName must be 1–50 characters");
        }
        this.value = value;
    }
}
