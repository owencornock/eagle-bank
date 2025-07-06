package com.eaglebank.eaglebanklogic.account;

import com.eaglebank.eaglebankdomain.account.*;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) {
        this.repo = repo;
    }

    public Account createAccount(
            UserId ownerId,
            AccountName name,
            AccountType type
    ) {
        Account account = Account.create(ownerId, name, type);
        return repo.save(account);
    }

    public List<Account> listAccounts(UserId ownerId) {
        return repo.findByOwner(ownerId);
    }

    public Account fetchAccount(AccountId id, UserId callerId) {
        Account account = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getOwnerId().equals(callerId)) {
            throw new ForbiddenException("Cannot fetch another user’s account");
        }
        return account;
    }

    @Transactional
    public Account updateAccount(
            AccountId id,
            UserId callerId,
            AccountName newName
    ) {
        Account existing = fetchAccount(id, callerId);
        Account updated = existing.withName(newName);
        return repo.save(updated);
    }

    public void deleteAccount(AccountId id, UserId callerId) {
        Account existing = fetchAccount(id, callerId);
        repo.delete(existing);
    }
}