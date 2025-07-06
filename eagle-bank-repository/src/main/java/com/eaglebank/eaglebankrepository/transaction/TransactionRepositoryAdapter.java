package com.eaglebank.eaglebankrepository.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.transaction.*;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final SpringDataTransactionRepository jpa;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction txn) {
        TransactionEntity entity = toEntity(txn);
        TransactionEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return jpa.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<Transaction> findByAccount(AccountId accountId) {
        return jpa.findByAccountId(accountId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionEntity toEntity(Transaction t) {
        return TransactionEntity.builder()
                .id(t.getId().value())
                .accountId(t.getAccountId().value())
                .type(t.getType().name())
                .amount(t.getAmount().value())
                .timestamp(t.getTimestamp())
                .currency(t.getCurrency().getCurrencyCode())  // Add this line
                .build();
    }

    private Transaction toDomain(TransactionEntity e) {
        return Transaction.rehydrate(
                TransactionId.of(e.getId()),
                AccountId.of(e.getAccountId()),
                TransactionType.valueOf(e.getType()),
                new Amount(e.getAmount()),
                e.getTimestamp(),
                Currency.getInstance(e.getCurrency())
        );
    }
}