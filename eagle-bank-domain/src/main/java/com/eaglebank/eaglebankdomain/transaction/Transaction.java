package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import lombok.Getter;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

@Getter
public final class Transaction {
    private final TransactionId id;
    private final AccountId accountId;
    private final TransactionType type;
    private final Amount amount;
    private final Instant timestamp;
    private final Currency currency;

    private Transaction(TransactionId id,
                        AccountId accountId,
                        TransactionType type,
                        Amount amount,
                        Instant timestamp,
                        Currency currency) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.type = Objects.requireNonNull(type);
        this.amount = Objects.requireNonNull(amount);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.currency = Objects.requireNonNull(currency);
    }

    public static Transaction create(
            AccountId accountId,
            TransactionType type,
            Amount amount,
            Currency currency
    ) {
        return new Transaction(
                TransactionId.newId(),
                accountId,
                type,
                amount,
                Instant.now(),
                currency
        );
    }

    public static Transaction rehydrate(
            TransactionId id,
            AccountId accountId,
            TransactionType type,
            Amount amount,
            Instant timestamp,
            Currency currency
    ) {
        return new Transaction(id, accountId, type, amount, timestamp, currency);
    }
}