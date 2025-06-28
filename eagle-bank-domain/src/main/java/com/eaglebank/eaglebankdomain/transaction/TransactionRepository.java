package com.eaglebank.eaglebankdomain.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(TransactionId id);
    List<Transaction> findByAccount(AccountId accountId);
}