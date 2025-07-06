package com.eaglebank.eaglebanklogic.transaction;

import com.eaglebank.eaglebankdomain.account.Account;
import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.account.AccountRepository;
import com.eaglebank.eaglebankdomain.account.Balance;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.transaction.*;
import com.eaglebank.eaglebankdomain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository txnRepo;
    private final AccountRepository accountRepo;

    public TransactionService(TransactionRepository txnRepo,
                              AccountRepository accountRepo) {
        this.txnRepo = txnRepo;
        this.accountRepo = accountRepo;
    }

    @Transactional
    public Transaction deposit(AccountId accountId, UserId callerId, Amount amount) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getOwnerId().equals(callerId)) {
            throw new ForbiddenException("Cannot deposit into another user's account");
        }

        Transaction txn = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                amount,
                account.getCurrency()
        );
        txnRepo.save(txn);

        Balance newBalance = new Balance(account.getBalance().value().add(amount.value()));
        accountRepo.save(account.withBalance(newBalance));

        return txn;
    }

    @Transactional
    public Transaction withdraw(AccountId accountId, UserId callerId, Amount amount) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getOwnerId().equals(callerId)) {
            throw new ForbiddenException("Cannot withdraw from another user's account");
        }

        if (account.getBalance().value().compareTo(amount.value()) < 0) {
            throw new InvalidUserDataException("Insufficient funds");
        }

        Transaction txn = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                account.getCurrency()
        );
        txnRepo.save(txn);

        Balance newBalance = new Balance(account.getBalance().value().subtract(amount.value()));
        accountRepo.save(account.withBalance(newBalance));

        return txn;
    }

    public List<Transaction> listTransactions(AccountId accountId, UserId callerId) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getOwnerId().equals(callerId)) {
            throw new ForbiddenException("Cannot list transactions on another user's account");
        }
        return txnRepo.findByAccount(accountId);
    }

    public Transaction fetchTransaction(AccountId accountId, TransactionId txnId, UserId callerId) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getOwnerId().equals(callerId)) {
            throw new ForbiddenException("Cannot fetch transaction on another user's account");
        }

        Transaction txn = txnRepo.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!txn.getAccountId().equals(accountId)) {
            throw new ResourceNotFoundException("Transaction not found for given account");
        }

        if (!txn.getCurrency().equals(account.getCurrency())) {
            throw new InvalidUserDataException("Transaction currency does not match account currency");
        }

        return txn;
    }
}