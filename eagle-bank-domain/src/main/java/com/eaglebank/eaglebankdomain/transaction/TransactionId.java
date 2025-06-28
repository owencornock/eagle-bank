package com.eaglebank.eaglebankdomain.transaction;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) {
    public static TransactionId newId()   { return new TransactionId(UUID.randomUUID()); }
    public static TransactionId of(UUID v) { return new TransactionId(Objects.requireNonNull(v)); }
}