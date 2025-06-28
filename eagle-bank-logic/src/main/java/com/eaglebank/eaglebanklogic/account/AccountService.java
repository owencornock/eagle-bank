package com.eaglebank.eaglebanklogic.account;

import com.eaglebank.eaglebankdomain.account.Account;
import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.account.AccountName;
import com.eaglebank.eaglebankdomain.account.AccountRepository;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) {
        this.repo = repo;
    }

    public Account createAccount(UserId ownerId, AccountName name) {
        Account account = Account.create(ownerId, name);
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

    public Account updateAccount(AccountId id, UserId callerId, AccountName newName) {
        Account existing = fetchAccount(id, callerId);
        Account updated = Account.rehydrate(
                existing.getId(),
                existing.getOwnerId(),
                newName,
                existing.getBalance()
        );
        return repo.save(updated);
    }

    public void deleteAccount(AccountId id, UserId callerId) {
        Account existing = fetchAccount(id, callerId);
        repo.delete(existing);
    }
}