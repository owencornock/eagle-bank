package com.eaglebank.eaglebankdomain.account;

import com.eaglebank.eaglebankdomain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(AccountId id);
    List<Account> findByOwner(UserId ownerId);
    void delete(Account account);
}
