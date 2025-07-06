package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    private AccountId accountId;
    private Amount amount;
    private TransactionType type;
    private Currency currency;

    @BeforeEach
    void setUp() {
        accountId = AccountId.newId();
        amount = new Amount(new BigDecimal("100.00"));
        type = TransactionType.DEPOSIT;
        currency = Currency.getInstance("GBP");
    }

    @Test
    void shouldCreateNewTransactionWithCurrentTimestamp() {
        Instant before = Instant.now();
        Transaction transaction = Transaction.create(accountId, type, amount, currency);
        Instant after = Instant.now();

        assertNotNull(transaction.getId(), "Transaction ID should be automatically generated");
        assertNotNull(transaction.getId().value(), "Underlying UUID should not be null");
        assertEquals(accountId, transaction.getAccountId(), "Account ID should match");
        assertEquals(type, transaction.getType(), "Transaction type should match");
        assertEquals(amount, transaction.getAmount(), "Amount should match");
        assertEquals(currency, transaction.getCurrency(), "Currency should match");

        Instant timestamp = transaction.getTimestamp();
        assertTrue(timestamp.isAfter(before) || timestamp.equals(before),
                "Timestamp should not be before transaction creation");
        assertTrue(timestamp.isBefore(after) || timestamp.equals(after),
                "Timestamp should not be after transaction creation");
    }

    @Test
    void shouldRehydrateTransactionWithAllValues() {
        TransactionId id = TransactionId.newId();
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        Transaction transaction = Transaction.rehydrate(id, accountId, type, amount, timestamp, currency);

        assertEquals(id, transaction.getId(), "Rehydrated transaction should preserve ID");
        assertEquals(accountId, transaction.getAccountId(),
                "Rehydrated transaction should preserve account ID");
        assertEquals(type, transaction.getType(),
                "Rehydrated transaction should preserve type");
        assertEquals(amount, transaction.getAmount(),
                "Rehydrated transaction should preserve amount");
        assertEquals(timestamp, transaction.getTimestamp(),
                "Rehydrated transaction should preserve timestamp");
        assertEquals(currency, transaction.getCurrency(),
                "Rehydrated transaction should preserve currency");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullValues() {
        assertThrows(NullPointerException.class, () -> {
            Transaction.create(null, type, amount, currency);
        }, "Should not accept null account ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.create(accountId, null, amount, currency);
        }, "Should not accept null transaction type");

        assertThrows(NullPointerException.class, () -> {
            Transaction.create(accountId, type, null, currency);
        }, "Should not accept null amount");

        assertThrows(NullPointerException.class, () -> {
            Transaction.create(accountId, type, amount, null);
        }, "Should not accept null currency");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRehydratingWithNullValues() {
        TransactionId id = TransactionId.newId();
        Instant timestamp = Instant.now();

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(null, accountId, type, amount, timestamp, currency);
        }, "Should not accept null ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, null, type, amount, timestamp, currency);
        }, "Should not accept null account ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, null, amount, timestamp, currency);
        }, "Should not accept null type");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, type, null, timestamp, currency);
        }, "Should not accept null amount");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, type, amount, null, currency);
        }, "Should not accept null timestamp");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, type, amount, timestamp, null);
        }, "Should not accept null currency");
    }

    @Test
    void shouldSupportWithdrawals() {
        Transaction withdrawal = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                currency
        );
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.getType(),
                "Should support withdrawal transactions");
    }

    @Test
    void shouldSupportDeposits() {
        Transaction deposit = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                amount,
                currency
        );
        assertEquals(TransactionType.DEPOSIT, deposit.getType(),
                "Should support deposit transactions");
    }

    @Test
    void shouldSupportDifferentCurrencies() {
        Currency usd = Currency.getInstance("USD");
        Currency eur = Currency.getInstance("EUR");

        Transaction gbpTransaction = Transaction.create(accountId, type, amount, currency);
        Transaction usdTransaction = Transaction.create(accountId, type, amount, usd);
        Transaction eurTransaction = Transaction.create(accountId, type, amount, eur);

        assertEquals(currency, gbpTransaction.getCurrency(), "Should support GBP currency");
        assertEquals(usd, usdTransaction.getCurrency(), "Should support USD currency");
        assertEquals(eur, eurTransaction.getCurrency(), "Should support EUR currency");
    }

    @Test
    void shouldBeImmutable() {
        Transaction transaction = Transaction.create(accountId, type, amount, currency);
        TransactionId originalId = transaction.getId();
        AccountId originalAccountId = transaction.getAccountId();
        TransactionType originalType = transaction.getType();
        Amount originalAmount = transaction.getAmount();
        Instant originalTimestamp = transaction.getTimestamp();
        Currency originalCurrency = transaction.getCurrency();

        transaction.getId().value();
        transaction.getAccountId().value();
        transaction.getAmount().value();
        transaction.getTimestamp();
        transaction.getCurrency();

        assertSame(originalId, transaction.getId(),
                "Transaction ID should be immutable");
        assertSame(originalAccountId, transaction.getAccountId(),
                "Account ID should be immutable");
        assertSame(originalType, transaction.getType(),
                "Transaction type should be immutable");
        assertSame(originalAmount, transaction.getAmount(),
                "Amount should be immutable");
        assertEquals(originalTimestamp, transaction.getTimestamp(),
                "Timestamp should be immutable");
        assertSame(originalCurrency, transaction.getCurrency(),
                "Currency should be immutable");
    }
}