package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    private AccountId accountId;
    private Amount amount;
    private TransactionType type;

    @BeforeEach
    void setUp() {
        accountId = AccountId.newId();
        amount = new Amount(new BigDecimal("100.00"));
        type = TransactionType.DEPOSIT;
    }

    @Test
    void shouldCreateNewTransactionWithCurrentTimestamp() {
        Instant before = Instant.now();
        Transaction transaction = Transaction.create(accountId, type, amount);
        Instant after = Instant.now();

        assertNotNull(transaction.getId(), "Transaction ID should be automatically generated");
        assertNotNull(transaction.getId().value(), "Underlying UUID should not be null");
        assertEquals(accountId, transaction.getAccountId(), "Account ID should match");
        assertEquals(type, transaction.getType(), "Transaction type should match");
        assertEquals(amount, transaction.getAmount(), "Amount should match");

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

        Transaction transaction = Transaction.rehydrate(id, accountId, type, amount, timestamp);

        assertEquals(id, transaction.getId(), "Rehydrated transaction should preserve ID");
        assertEquals(accountId, transaction.getAccountId(),
                "Rehydrated transaction should preserve account ID");
        assertEquals(type, transaction.getType(),
                "Rehydrated transaction should preserve type");
        assertEquals(amount, transaction.getAmount(),
                "Rehydrated transaction should preserve amount");
        assertEquals(timestamp, transaction.getTimestamp(),
                "Rehydrated transaction should preserve timestamp");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullValues() {
        assertThrows(NullPointerException.class, () -> {
            Transaction.create(null, type, amount);
        }, "Should not accept null account ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.create(accountId, null, amount);
        }, "Should not accept null transaction type");

        assertThrows(NullPointerException.class, () -> {
            Transaction.create(accountId, type, null);
        }, "Should not accept null amount");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRehydratingWithNullValues() {
        TransactionId id = TransactionId.newId();
        Instant timestamp = Instant.now();

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(null, accountId, type, amount, timestamp);
        }, "Should not accept null ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, null, type, amount, timestamp);
        }, "Should not accept null account ID");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, null, amount, timestamp);
        }, "Should not accept null type");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, type, null, timestamp);
        }, "Should not accept null amount");

        assertThrows(NullPointerException.class, () -> {
            Transaction.rehydrate(id, accountId, type, amount, null);
        }, "Should not accept null timestamp");
    }

    @Test
    void shouldSupportWithdrawals() {
        Transaction withdrawal = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                amount
        );
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.getType(),
                "Should support withdrawal transactions");
    }

    @Test
    void shouldSupportDeposits() {
        Transaction deposit = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                amount
        );
        assertEquals(TransactionType.DEPOSIT, deposit.getType(),
                "Should support deposit transactions");
    }

    @Test
    void shouldBeImmutable() {
        Transaction transaction = Transaction.create(accountId, type, amount);
        TransactionId originalId = transaction.getId();
        AccountId originalAccountId = transaction.getAccountId();
        TransactionType originalType = transaction.getType();
        Amount originalAmount = transaction.getAmount();
        Instant originalTimestamp = transaction.getTimestamp();

        // Try to modify state through getters (if they were to return mutable objects)
        transaction.getId().value();
        transaction.getAccountId().value();
        transaction.getAmount().value();
        transaction.getTimestamp();

        // Verify nothing changed
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
    }
}