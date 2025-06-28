package com.eaglebank.eaglebanklogic.transaction;

import com.eaglebank.eaglebankdomain.account.*;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.transaction.*;
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

class TransactionServiceTest {

    @Mock
    private TransactionRepository txnRepo;
    @Mock
    private AccountRepository accountRepo;
    private TransactionService service;

    // shared fields
    private UserId ownerId;
    private AccountId accountId;
    private Account account;
    private Amount amount;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new TransactionService(txnRepo, accountRepo);

        // initialize test data
        ownerId = UserId.newId();
        accountId = AccountId.newId();
        account = Account.create(ownerId, new AccountName("Test Account"));
        amount = new Amount(new BigDecimal("100.00"));

        // Default mock behavior
        when(txnRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldProcessDepositWhenAccountExists() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

        Transaction result = service.deposit(accountId, ownerId, amount);

        assertNotNull(result.getId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(amount, result.getAmount());

        verify(txnRepo).save(any(Transaction.class));
        verify(accountRepo).save(any(Account.class));
    }

    @Test
    void shouldUpdateBalanceAfterDeposit() {
        BigDecimal initialBalance = new BigDecimal("50.00");
        Account accountWithBalance = Account.rehydrate(
                accountId, ownerId, account.getName(),
                new Balance(initialBalance)
        );
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(accountWithBalance));

        service.deposit(accountId, ownerId, amount);

        verify(accountRepo).save(argThat(updatedAccount ->
                updatedAccount.getBalance().value()
                        .equals(initialBalance.add(amount.value()))));
    }

    @Test
    void shouldProcessWithdrawalWhenSufficientFunds() {
        Account accountWithBalance = Account.rehydrate(
                accountId, ownerId, account.getName(),
                new Balance(new BigDecimal("200.00"))
        );
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(accountWithBalance));

        Transaction result = service.withdraw(accountId, ownerId, amount);

        assertNotNull(result.getId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(amount, result.getAmount());

        verify(txnRepo).save(any(Transaction.class));
        verify(accountRepo).save(any(Account.class));
    }

    @Test
    void shouldThrowInvalidUserDataExceptionWhenInsufficientFunds() {
        Account accountWithLowBalance = Account.rehydrate(
                accountId, ownerId, account.getName(),
                new Balance(new BigDecimal("50.00"))
        );
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(accountWithLowBalance));

        assertThrows(InvalidUserDataException.class, () ->
                service.withdraw(accountId, ownerId, amount)
        );

        verify(txnRepo, never()).save(any());
        verify(accountRepo, never()).save(any());
    }

    @Test
    void shouldListTransactionsForAccountOwner() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        List<Transaction> transactions = List.of(
                Transaction.create(accountId, TransactionType.DEPOSIT, amount),
                Transaction.create(accountId, TransactionType.WITHDRAWAL, amount)
        );
        when(txnRepo.findByAccount(accountId)).thenReturn(transactions);

        List<Transaction> result = service.listTransactions(accountId, ownerId);

        assertEquals(2, result.size());
        verify(txnRepo).findByAccount(accountId);
    }

    @Test
    void shouldFetchTransactionWhenAuthorized() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        Transaction transaction = Transaction.create(accountId, TransactionType.DEPOSIT, amount);
        TransactionId txnId = transaction.getId();
        when(txnRepo.findById(txnId)).thenReturn(Optional.of(transaction));

        Transaction result = service.fetchTransaction(accountId, txnId, ownerId);

        assertSame(transaction, result);
        verify(txnRepo).findById(txnId);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenDepositingToOtherUsersAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.deposit(accountId, otherUserId, amount)
        );

        verify(txnRepo, never()).save(any());
        verify(accountRepo, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenWithdrawingFromOtherUsersAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.withdraw(accountId, otherUserId, amount)
        );

        verify(txnRepo, never()).save(any());
        verify(accountRepo, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.deposit(accountId, ownerId, amount)
        );

        verify(txnRepo, never()).save(any());
        verify(accountRepo, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTransactionDoesNotExist() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        TransactionId txnId = TransactionId.newId();
        when(txnRepo.findById(txnId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.fetchTransaction(accountId, txnId, ownerId)
        );
    }

    @Test
    void shouldThrowResourceNotFoundWhenTransactionBelongsToOtherAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        AccountId otherAccountId = AccountId.newId();
        Transaction transaction = Transaction.create(otherAccountId, TransactionType.DEPOSIT, amount);
        when(txnRepo.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThrows(ResourceNotFoundException.class, () ->
                service.fetchTransaction(accountId, transaction.getId(), ownerId)
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenListingTransactionsForNonExistentAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.listTransactions(accountId, ownerId)
        );

        verify(txnRepo, never()).findByAccount(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenListingTransactionsForOtherUsersAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.listTransactions(accountId, otherUserId)
        );

        verify(txnRepo, never()).findByAccount(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenFetchingTransactionFromOtherUsersAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        TransactionId txnId = TransactionId.newId();
        UserId otherUserId = UserId.newId();

        assertThrows(ForbiddenException.class, () ->
                service.fetchTransaction(accountId, txnId, otherUserId)
        );

        verify(txnRepo, never()).findById(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFetchingTransactionFromNonExistentAccount() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());
        TransactionId txnId = TransactionId.newId();

        assertThrows(ResourceNotFoundException.class, () ->
                service.fetchTransaction(accountId, txnId, ownerId)
        );

        verify(txnRepo, never()).findById(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAccountDoesNotExistOnWithdrawal() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());
        Amount withdrawalAmount = new Amount(new BigDecimal("50.00"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.withdraw(accountId, ownerId, withdrawalAmount)
        );

        assertEquals("Account not found", exception.getMessage());

        verify(txnRepo, never()).save(any());
        verify(accountRepo, never()).save(any());
    }
}