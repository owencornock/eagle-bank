package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class LastName {
    @NonNull
    private final String value;

    public LastName(String value) {
        if (value.isBlank() || value.length() > 50) {
            throw new InvalidUserDataException("LastName must be 1–50 characters");
        }
        this.value = value;
    }
}
