package com.eaglebank.eaglebankrepository.account;

import com.eaglebank.eaglebankdomain.account.*;
import com.eaglebank.eaglebankdomain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {
    private final SpringDataAccountRepository jpa;

    public AccountRepositoryAdapter(SpringDataAccountRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpa.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<Account> findByOwner(UserId ownerId) {
        return jpa.findByUserId(ownerId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(Account account) {
        jpa.deleteById(account.getId().value());
    }

    private AccountEntity toEntity(Account a) {
        return AccountEntity.builder()
                .id(a.getId().value())
                .userId(a.getOwnerId().value())
                .name(a.getName().value())
                .balance(a.getBalance().value())
                .accountNumber(a.getAccountNumber().value())
                .sortCode(a.getSortCode().value())
                .type(a.getType())
                .currency(a.getCurrency().getCurrencyCode())
                .createdTimestamp(a.getCreatedTimestamp())
                .updatedTimestamp(a.getUpdatedTimestamp())
                .build();
    }

    private Account toDomain(AccountEntity e) {
        return Account.rehydrate(
                AccountId.of(e.getId()),
                UserId.of(e.getUserId()),
                new AccountName(e.getName()),
                new Balance(e.getBalance()),
                new AccountNumber(e.getAccountNumber()),
                new SortCode(e.getSortCode()),
                e.getType(),
                Currency.getInstance(e.getCurrency()),
                e.getCreatedTimestamp(),
                e.getUpdatedTimestamp()
        );
    }
}