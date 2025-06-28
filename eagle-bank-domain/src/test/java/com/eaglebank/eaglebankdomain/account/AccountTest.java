package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateNewAccountWithZeroBalance() {
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");

        Account account = Account.create(ownerId, name);

        assertNotNull(account.getId(), "Account ID should be automatically generated");
        assertEquals(ownerId, account.getOwnerId(), "Owner ID should match");
        assertEquals(name, account.getName(), "Account name should match");
        assertEquals(new BigDecimal("0.00"), account.getBalance().value(),
                "New account should have zero balance");
    }

    @Test
    void shouldRehydrateAccountWithAllValues() {
        AccountId id = AccountId.newId();
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");
        Balance balance = new Balance(new BigDecimal("100.00"));

        Account account = Account.rehydrate(id, ownerId, name, balance);

        assertEquals(id, account.getId(), "Rehydrated account should preserve ID");
        assertEquals(ownerId, account.getOwnerId(), "Rehydrated account should preserve owner ID");
        assertEquals(name, account.getName(), "Rehydrated account should preserve name");
        assertEquals(balance, account.getBalance(), "Rehydrated account should preserve balance");
    }

    @Test
    void shouldCreateNewAccountWithNewBalance() {
        Account original = Account.create(
                UserId.of(UUID.randomUUID()),
                new AccountName("Test Account")
        );
        Balance newBalance = new Balance(new BigDecimal("50.00"));

        Account modified = original.withBalance(newBalance);

        assertEquals(original.getId(), modified.getId(), "Account ID should not change");
        assertEquals(original.getOwnerId(), modified.getOwnerId(), "Owner ID should not change");
        assertEquals(original.getName(), modified.getName(), "Name should not change");
        assertEquals(newBalance, modified.getBalance(), "Balance should be updated");
        assertNotSame(original, modified, "Should create new instance");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullOwner() {
        AccountName name = new AccountName("Test Account");

        assertThrows(NullPointerException.class, () -> {
            Account.create(null, name);
        }, "Should not accept null owner ID");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullName() {
        UserId ownerId = UserId.of(UUID.randomUUID());

        assertThrows(NullPointerException.class, () -> {
            Account.create(ownerId, null);
        }, "Should not accept null account name");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRehydratingWithNullValues() {
        AccountId id = AccountId.newId();
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");
        Balance balance = new Balance(new BigDecimal("100.00"));

        assertThrows(NullPointerException.class, () -> {
            Account.rehydrate(null, ownerId, name, balance);
        }, "Should not accept null ID");

        assertThrows(NullPointerException.class, () -> {
            Account.rehydrate(id, null, name, balance);
        }, "Should not accept null owner ID");

        assertThrows(NullPointerException.class, () -> {
            Account.rehydrate(id, ownerId, null, balance);
        }, "Should not accept null name");

        assertThrows(NullPointerException.class, () -> {
            Account.rehydrate(id, ownerId, name, null);
        }, "Should not accept null balance");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenUpdatingWithNullBalance() {
        Account original = Account.create(
                UserId.of(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        assertThrows(NullPointerException.class, () -> {
            original.withBalance(null);
        }, "Should not accept null balance");
    }

    @Test
    void shouldHandleDifferentOwnerIds() {
        AccountName name = new AccountName("Test Account");
        Account account1 = Account.create(
                UserId.of(UUID.randomUUID()),
                name
        );
        Account account2 = Account.rehydrate(
                account1.getId(),
                UserId.of(UUID.randomUUID()),
                account1.getName(),
                account1.getBalance()
        );

        assertNotEquals(account1, account2,
                "Accounts with same ID but different owners should not be equal");
    }

    @Test
    void shouldHandleDifferentNames() {
        UserId ownerId = UserId.of(UUID.randomUUID());
        Account account1 = Account.create(
                ownerId,
                new AccountName("First Account")
        );
        Account account2 = Account.rehydrate(
                account1.getId(),
                account1.getOwnerId(),
                new AccountName("Second Account"),
                account1.getBalance()
        );

        assertNotEquals(account1, account2,
                "Accounts with same ID but different names should not be equal");
    }

    @Test
    void shouldHandleDifferentBalances() {
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");
        Account account1 = Account.create(ownerId, name);
        Account account2 = Account.rehydrate(
                account1.getId(),
                account1.getOwnerId(),
                account1.getName(),
                new Balance(new BigDecimal("100.00"))
        );

        assertNotEquals(account1, account2,
                "Accounts with same ID but different balances should not be equal");
    }

    @Test
    void shouldPreserveImmutabilityWhenUpdatingBalance() {
        Account original = Account.create(
                UserId.of(UUID.randomUUID()),
                new AccountName("Test Account")
        );
        Balance firstBalance = new Balance(new BigDecimal("50.00"));
        Balance secondBalance = new Balance(new BigDecimal("100.00"));

        Account firstModification = original.withBalance(firstBalance);
        Account secondModification = firstModification.withBalance(secondBalance);

        assertEquals(new BigDecimal("0.00"), original.getBalance().value(),
                "Original account should maintain zero balance");
        assertEquals(firstBalance, firstModification.getBalance(),
                "First modification should have first balance");
        assertEquals(secondBalance, secondModification.getBalance(),
                "Second modification should have second balance");
        assertNotSame(firstModification, secondModification,
                "Each modification should create new instance");
    }
}