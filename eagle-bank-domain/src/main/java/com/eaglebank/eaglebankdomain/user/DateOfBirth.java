package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;

@Getter
public class DateOfBirth {
    @NonNull
    private final LocalDate value;

    public DateOfBirth(LocalDate value) {
        if (value == null) {
            throw new InvalidUserDataException("DateOfBirth cannot be null");
        }
        if (value.isAfter(LocalDate.now().minusYears(18))) {
            throw new InvalidUserDataException("User must be at least 18 years old");
        }
        this.value = value;
    }
}
