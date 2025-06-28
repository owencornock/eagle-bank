package com.eaglebank.eaglebankdomain.account;


import com.eaglebank.eaglebankdomain.user.UserId;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class Account {
    private final AccountId id;
    private final UserId ownerId;
    private final AccountName name;
    private final Balance balance;

    private Account(AccountId id,
                    UserId ownerId,
                    AccountName name,
                    Balance balance) {
        this.id = Objects.requireNonNull(id, "AccountId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.name = Objects.requireNonNull(name, "AccountName cannot be null");
        this.balance = Objects.requireNonNull(balance, "Balance cannot be null");
    }

    public static Account create(UserId ownerId, AccountName name) {
        return new Account(
                AccountId.newId(),
                ownerId,
                name,
                new Balance(java.math.BigDecimal.ZERO)
        );
    }


    public static Account rehydrate(
            AccountId id,
            UserId ownerId,
            AccountName name,
            Balance balance) {
        return new Account(id, ownerId, name, balance);
    }

    public Account withBalance(Balance newBalance) {
        return new Account(id, ownerId, name, newBalance);
    }
}