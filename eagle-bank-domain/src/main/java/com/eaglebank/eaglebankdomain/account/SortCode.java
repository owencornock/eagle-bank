package com.eaglebank.eaglebankdomain.account;

import java.util.Objects;


public record SortCode(String value) {
    public SortCode(String value) {
        this.value = Objects.requireNonNull(value, "Sort code cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Sort code cannot be empty");
        }
        if (!value.matches("\\d{6}")) {
            throw new IllegalArgumentException("Sort code must be exactly 6 digits");
        }
    }

}