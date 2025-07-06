package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import java.util.Objects;

public record PhoneNumber(String value) {
    private static final String PHONE_PATTERN = "^\\+[1-9]\\d{1,14}$";
    
    public PhoneNumber(String value) {
        this.value = Objects.requireNonNull(value, "Phone number cannot be null");
        
        if (value.trim().isEmpty()) {
            throw new InvalidUserDataException("Phone number cannot be empty");
        }
        
        if (!value.matches(PHONE_PATTERN)) {
            throw new InvalidUserDataException(
                "Phone number must start with '+' followed by 2-15 digits");
        }
    }
}