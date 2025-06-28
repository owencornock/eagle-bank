package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public final class Transaction {
    private final TransactionId id;
    private final AccountId accountId;
    private final TransactionType type;
    private final Amount amount;
    private final Instant timestamp;

    private Transaction(TransactionId id,
                        AccountId accountId,
                        TransactionType type,
                        Amount amount,
                        Instant timestamp) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.type = Objects.requireNonNull(type);
        this.amount = Objects.requireNonNull(amount);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public static Transaction create(
            AccountId accountId,
            TransactionType type,
            Amount amount
    ) {
        return new Transaction(
                TransactionId.newId(),
                accountId,
                type,
                amount,
                Instant.now()
        );
    }

    public static Transaction rehydrate(
            TransactionId id,
            AccountId accountId,
            TransactionType type,
            Amount amount,
            Instant timestamp
    ) {
        return new Transaction(id, accountId, type, amount, timestamp);
    }
}