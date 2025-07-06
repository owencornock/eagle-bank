package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.user.UserId;
import lombok.Getter;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.Random;

@Getter
public final class Account {
    private static final String BANK_SORT_CODE = "123456";
    private static final Random RANDOM = new Random();

    private final AccountId id;
    private final UserId ownerId;
    private final AccountName name;
    private final Balance balance;
    private final AccountNumber accountNumber;
    private final SortCode sortCode;
    private final AccountType type;
    private final Currency currency;
    private final Instant createdTimestamp;
    private final Instant updatedTimestamp;

    private Account(AccountId id,
                   UserId ownerId,
                   AccountName name,
                   Balance balance,
                   AccountNumber accountNumber,
                   SortCode sortCode,
                   AccountType type,
                   Currency currency,
                   Instant createdTimestamp,
                   Instant updatedTimestamp) {
        this.id = Objects.requireNonNull(id, "AccountId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.name = Objects.requireNonNull(name, "AccountName cannot be null");
        this.balance = Objects.requireNonNull(balance, "Balance cannot be null");
        this.accountNumber = Objects.requireNonNull(accountNumber, "AccountNumber cannot be null");
        this.sortCode = Objects.requireNonNull(sortCode, "SortCode cannot be null");
        this.type = Objects.requireNonNull(type, "AccountType cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.createdTimestamp = Objects.requireNonNull(createdTimestamp, "CreatedTimestamp cannot be null");
        this.updatedTimestamp = Objects.requireNonNull(updatedTimestamp, "UpdatedTimestamp cannot be null");
    }

    public static Account create(UserId ownerId,
                                 AccountName name,
                                 AccountType type) {
        Instant now = Instant.now();
        return new Account(
                AccountId.newId(),
                ownerId,
                name,
                new Balance(java.math.BigDecimal.ZERO),
                generateAccountNumber(),
                new SortCode(BANK_SORT_CODE),
                type,
                Currency.getInstance("GBP"),  // Default currency
                now,
                now
        );
    }


    public static Account rehydrate(
            AccountId id,
            UserId ownerId,
            AccountName name,
            Balance balance,
            AccountNumber accountNumber,
            SortCode sortCode,
            AccountType type,
            Currency currency,
            Instant createdTimestamp,
            Instant updatedTimestamp) {
        return new Account(id, ownerId, name, balance, accountNumber, sortCode, 
                         type, currency, createdTimestamp, updatedTimestamp);
    }

    public Account withBalance(Balance newBalance) {
        return new Account(id, ownerId, name, newBalance, accountNumber, sortCode, 
                         type, currency, createdTimestamp, Instant.now());
    }

    public Account withName(AccountName accountName) {
        return new Account(
                this.id,
                this.ownerId,
                accountName,
                this.balance,
                this.accountNumber,
                this.sortCode,
                this.type,
                this.currency,
                this.createdTimestamp,
                Instant.now()
        );
    }

    private static AccountNumber generateAccountNumber() {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            number.append(RANDOM.nextInt(10));
        }
        return new AccountNumber(number.toString());
    }

}