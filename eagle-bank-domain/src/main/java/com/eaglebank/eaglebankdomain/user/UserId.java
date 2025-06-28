package com.eaglebank.eaglebankdomain.user;

import java.util.Objects;
import java.util.UUID;


public record UserId(UUID value) {

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Wrap an existing UUID.
     */
    public static UserId of(UUID id) {
        return new UserId(Objects.requireNonNull(id));
    }
}
