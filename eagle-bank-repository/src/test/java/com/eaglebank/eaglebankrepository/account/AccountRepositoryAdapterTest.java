package com.eaglebank.eaglebankrepository.account;

import com.eaglebank.eaglebankdomain.account.*;
import com.eaglebank.eaglebankdomain.user.UserId;
import com.eaglebank.eaglebankrepository.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
class AccountRepositoryAdapterTest {

    @Autowired
    private AccountRepository repo;

    @Test
    void shouldSaveAndFindAccountById() {
        Account account = Account.create(
                UserId.newId(),
                new AccountName("Test Account"),
                AccountType.SAVINGS
        );
        Account saved = repo.save(account);
        assertThat(saved.getName().value()).isEqualTo("Test Account");
        assertThat(saved.getBalance().value()).isEqualTo(new BigDecimal("0.00"));
        assertThat(saved.getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(saved.getCurrency()).isEqualTo(Currency.getInstance("GBP"));

        Optional<Account> fetched = repo.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getName().value()).isEqualTo("Test Account");
        assertThat(fetched.get().getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(fetched.get().getCurrency()).isEqualTo(Currency.getInstance("GBP"));
    }

    @Test
    void shouldFindAccountsByOwner() {
        UserId ownerId = UserId.newId();
        Account account1 = Account.create(ownerId, new AccountName("First Account"), AccountType.CHECKING);
        Account account2 = Account.create(ownerId, new AccountName("Second Account"), AccountType.SAVINGS);

        repo.save(account1);
        repo.save(account2);
        repo.save(Account.create(UserId.newId(), new AccountName("Other User Account"), AccountType.BUSINESS));

        List<Account> ownerAccounts = repo.findByOwner(ownerId);
        assertThat(ownerAccounts).hasSize(2);
        assertThat(ownerAccounts)
                .extracting(a -> a.getName().value())
                .containsExactlyInAnyOrder("First Account", "Second Account");
        assertThat(ownerAccounts)
                .extracting(Account::getType)
                .containsExactlyInAnyOrder(AccountType.CHECKING, AccountType.SAVINGS);
    }

    @Test
    void shouldUpdateExistingAccount() {
        Account original = repo.save(
                Account.create(UserId.newId(), new AccountName("Original Name"), AccountType.CHECKING)
        );

        Account updated = Account.rehydrate(
                original.getId(),
                original.getOwnerId(),
                new AccountName("Updated Name"),
                new Balance(new BigDecimal("100.00")),
                original.getAccountNumber(),
                original.getSortCode(),
                AccountType.SAVINGS,
                Currency.getInstance("GBP"),
                original.getCreatedTimestamp(),
                Instant.now()
        );
        repo.save(updated);

        Optional<Account> fetched = repo.findById(original.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getName().value()).isEqualTo("Updated Name");
        assertThat(fetched.get().getBalance().value())
                .isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(fetched.get().getType()).isEqualTo(AccountType.SAVINGS);
    }

    @Test
    void shouldPreserveAllAccountFields() {
        AccountId id = AccountId.of(UUID.randomUUID());
        UserId ownerId = UserId.of(UUID.randomUUID());
        AccountName name = new AccountName("Test Account");
        Balance balance = new Balance(new BigDecimal("123.45"));
        AccountNumber accountNumber = new AccountNumber("12345678");
        SortCode sortCode = new SortCode("123456");
        AccountType type = AccountType.BUSINESS;
        Currency currency = Currency.getInstance("GBP");
        Instant now = Instant.now();

        Account original = Account.rehydrate(id, ownerId, name, balance, accountNumber, 
                sortCode, type, currency, now, now);
        Account saved = repo.save(original);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getName().value()).isEqualTo(name.value());
        assertThat(saved.getBalance().value()).isEqualByComparingTo(balance.value());
        assertThat(saved.getAccountNumber().value()).isEqualTo(accountNumber.value());
        assertThat(saved.getSortCode().value()).isEqualTo(sortCode.value());
        assertThat(saved.getType()).isEqualTo(type);
        assertThat(saved.getCurrency()).isEqualTo(currency);
        assertThat(saved.getCreatedTimestamp()).isEqualTo(now);
        assertThat(saved.getUpdatedTimestamp()).isEqualTo(now);

        Optional<Account> fetched = repo.findById(id);
        assertThat(fetched).isPresent();
        Account fetchedAccount = fetched.get();
        assertThat(fetchedAccount.getId()).isEqualTo(id);
        assertThat(fetchedAccount.getOwnerId()).isEqualTo(ownerId);
        assertThat(fetchedAccount.getName().value()).isEqualTo(name.value());
        assertThat(fetchedAccount.getBalance().value()).isEqualByComparingTo(balance.value());
        assertThat(fetchedAccount.getAccountNumber().value()).isEqualTo(accountNumber.value());
        assertThat(fetchedAccount.getSortCode().value()).isEqualTo(sortCode.value());
        assertThat(fetchedAccount.getType()).isEqualTo(type);
        assertThat(fetchedAccount.getCurrency()).isEqualTo(currency);
        assertThat(fetchedAccount.getCreatedTimestamp()).isEqualTo(now);
        assertThat(fetchedAccount.getUpdatedTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldDeleteAccount() {
        Account account = repo.save(
                Account.create(UserId.newId(), new AccountName("To Delete"), AccountType.CHECKING)
        );

        repo.delete(account);

        Optional<Account> fetched = repo.findById(account.getId());
        assertThat(fetched).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<Account> result = repo.findById(AccountId.newId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsForOwner() {
        List<Account> accounts = repo.findByOwner(UserId.newId());
        assertThat(accounts).isEmpty();
    }
}