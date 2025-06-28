package com.eaglebank.eaglebankdomain.account;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {
    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(UUID id) {
        return new AccountId(Objects.requireNonNull(id));
    }

}