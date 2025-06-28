package com.eaglebank.eaglebanklogic.account;

import com.eaglebank.eaglebankdomain.account.Account;
import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.account.AccountName;
import com.eaglebank.eaglebankdomain.account.AccountRepository;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository repo;
    private AccountService service;

    // shared fields
    private UserId ownerId;
    private AccountName accountName;
    private Account existingAccount;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new AccountService(repo);

        // initialize test data
        ownerId = UserId.newId();
        accountName = new AccountName("Test Account");
        existingAccount = Account.create(ownerId, accountName);

        // Default mock behavior
        when(repo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldCreateAndSaveAccount() {
        Account result = service.createAccount(ownerId, accountName);

        assertNotNull(result.getId(), "Account ID should be generated");
        assertEquals(ownerId, result.getOwnerId(), "Owner ID should match");
        assertEquals(accountName, result.getName(), "Account name should match");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getBalance().value(),
                "New account should have zero balance");

        verify(repo).save(any(Account.class));
    }

    @Test
    void shouldListAccountsForOwner() {
        List<Account> accounts = List.of(
                Account.create(ownerId, new AccountName("Account 1")),
                Account.create(ownerId, new AccountName("Account 2"))
        );
        when(repo.findByOwner(ownerId)).thenReturn(accounts);

        List<Account> result = service.listAccounts(ownerId);

        assertEquals(2, result.size(), "Should return all accounts");
        verify(repo).findByOwner(ownerId);
    }

    @Test
    void shouldFetchAccountWhenOwnerMatches() {
        AccountId id = existingAccount.getId();
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));

        Account result = service.fetchAccount(id, ownerId);

        assertSame(existingAccount, result);
        verify(repo).findById(id);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenFetchingOtherUsersAccount() {
        AccountId id = existingAccount.getId();
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.fetchAccount(id, otherUserId)
        );
        verify(repo).findById(id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAccountDoesNotExist() {
        AccountId id = AccountId.newId();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.fetchAccount(id, ownerId)
        );
        verify(repo).findById(id);
    }

    @Test
    void shouldUpdateAccountNameWhenOwnerMatches() {
        AccountId id = existingAccount.getId();
        AccountName newName = new AccountName("Updated Account");
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));

        Account result = service.updateAccount(id, ownerId, newName);

        assertEquals(newName, result.getName(), "Account name should be updated");
        assertEquals(existingAccount.getId(), result.getId(), "ID should remain same");
        assertEquals(existingAccount.getOwnerId(), result.getOwnerId(),
                "Owner should remain same");
        assertEquals(existingAccount.getBalance(), result.getBalance(),
                "Balance should remain same");

        verify(repo).findById(id);
        verify(repo).save(any(Account.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUpdatingOtherUsersAccount() {
        AccountId id = existingAccount.getId();
        AccountName newName = new AccountName("Updated Account");
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.updateAccount(id, otherUserId, newName)
        );
        verify(repo).findById(id);
        verify(repo, never()).save(any());
    }

    @Test
    void shouldDeleteAccountWhenOwnerMatches() {
        AccountId id = existingAccount.getId();
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));

        service.deleteAccount(id, ownerId);

        verify(repo).findById(id);
        verify(repo).delete(existingAccount);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenDeletingOtherUsersAccount() {
        AccountId id = existingAccount.getId();
        when(repo.findById(id)).thenReturn(Optional.of(existingAccount));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.deleteAccount(id, otherUserId)
        );
        verify(repo).findById(id);
        verify(repo, never()).delete(any());
    }
}