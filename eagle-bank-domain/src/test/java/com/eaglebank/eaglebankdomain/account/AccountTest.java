package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.user.UserId;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateNewAccountWithZeroBalance() {
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");

        Account account = Account.create(ownerId, name, AccountType.CHECKING);

        assertNotNull(account.getId(), "Account ID should be automatically generated");
        assertEquals(ownerId, account.getOwnerId(), "Owner ID should match");
        assertEquals(name, account.getName(), "Account name should match");
        assertEquals(new BigDecimal("0.00"), account.getBalance().value(),
                "New account should have zero balance");
        assertEquals("123456", account.getSortCode().value(),
                "Sort code should match bank's code");
        assertNotNull(account.getAccountNumber(), "Account number should be generated");
        assertTrue(account.getAccountNumber().value().matches("\\d{8}"),
                "Account number should be 8 digits");
        assertEquals(AccountType.CHECKING, account.getType(), "Account type should match");
        assertEquals(Currency.getInstance("GBP"), account.getCurrency(),
                "Currency should be GBP");
        assertNotNull(account.getCreatedTimestamp(), "Created timestamp should be set");
        assertNotNull(account.getUpdatedTimestamp(), "Updated timestamp should be set");
    }

    @Test
    void shouldRehydrateAccountWithAllValues() {
        AccountId id = AccountId.newId();
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");
        Balance balance = new Balance(new BigDecimal("100.00"));
        AccountNumber accountNumber = new AccountNumber("12345678");
        SortCode sortCode = new SortCode("123456");
        AccountType type = AccountType.SAVINGS;
        Currency currency = Currency.getInstance("GBP");
        Instant now = Instant.now();

        Account account = Account.rehydrate(id, ownerId, name, balance, accountNumber, 
                sortCode, type, currency, now, now);

        assertEquals(id, account.getId(), "Rehydrated account should preserve ID");
        assertEquals(ownerId, account.getOwnerId(), "Rehydrated account should preserve owner ID");
        assertEquals(name, account.getName(), "Rehydrated account should preserve name");
        assertEquals(balance, account.getBalance(), "Rehydrated account should preserve balance");
        assertEquals(accountNumber, account.getAccountNumber(), "Rehydrated account should preserve account number");
        assertEquals(sortCode, account.getSortCode(), "Rehydrated account should preserve sort code");
        assertEquals(type, account.getType(), "Rehydrated account should preserve type");
        assertEquals(currency, account.getCurrency(), "Rehydrated account should preserve currency");
        assertEquals(now, account.getCreatedTimestamp(), "Rehydrated account should preserve created timestamp");
        assertEquals(now, account.getUpdatedTimestamp(), "Rehydrated account should preserve updated timestamp");
    }

    @Test
    void shouldCreateNewAccountWithNewBalance() {
        Account original = Account.create(
                UserId.of(UUID.randomUUID()),
                new AccountName("Test Account"),
                AccountType.CHECKING
        );
        Balance newBalance = new Balance(new BigDecimal("50.00"));

        Account modified = original.withBalance(newBalance);

        assertEquals(original.getId(), modified.getId(), "Account ID should not change");
        assertEquals(original.getOwnerId(), modified.getOwnerId(), "Owner ID should not change");
        assertEquals(original.getName(), modified.getName(), "Name should not change");
        assertEquals(newBalance, modified.getBalance(), "Balance should be updated");
        assertEquals(original.getAccountNumber(), modified.getAccountNumber(), "Account number should not change");
        assertEquals(original.getSortCode(), modified.getSortCode(), "Sort code should not change");
        assertEquals(original.getType(), modified.getType(), "Type should not change");
        assertEquals(original.getCurrency(), modified.getCurrency(), "Currency should not change");
        assertEquals(original.getCreatedTimestamp(), modified.getCreatedTimestamp(), "Created timestamp should not change");
        assertTrue(modified.getUpdatedTimestamp().isAfter(original.getUpdatedTimestamp()), 
                "Updated timestamp should be newer");
        assertNotSame(original, modified, "Should create new instance");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullOwner() {
        AccountName name = new AccountName("Test Account");

        assertThrows(NullPointerException.class, () ->
                Account.create(null, name, AccountType.CHECKING),
                "Should not accept null owner ID");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullName() {
        UserId ownerId = UserId.of(UUID.randomUUID());

        assertThrows(NullPointerException.class, () ->
                Account.create(ownerId, null, AccountType.CHECKING),
                "Should not accept null account name");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingWithNullType() {
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");

        assertThrows(NullPointerException.class, () ->
                Account.create(ownerId, name, null),
                "Should not accept null account type");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenUpdatingWithNullBalance() {
        Account original = Account.create(
                UserId.of(UUID.randomUUID()),
                new AccountName("Test Account"),
                AccountType.CHECKING
        );

        assertThrows(NullPointerException.class, () ->
                original.withBalance(null),
                "Should not accept null balance");
    }
}