package com.eaglebank.eaglebankrepository.transaction;

import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.transaction.*;
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
class TransactionRepositoryAdapterTest {

    @Autowired
    private TransactionRepository repo;

    @Test
    void shouldSaveAndFindTransactionById() {
        Transaction txn = Transaction.create(
                AccountId.newId(),
                TransactionType.DEPOSIT,
                new Amount(new BigDecimal("100.00")),
                Currency.getInstance("GBP")
        );
        Transaction saved = repo.save(txn);

        assertThat(saved.getAmount().value())
                .isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(saved.getType()).isEqualTo(TransactionType.DEPOSIT);

        Optional<Transaction> fetched = repo.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getAmount().value())
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldFindTransactionsByAccount() {
        AccountId accountId = AccountId.newId();
        Transaction deposit = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                new Amount(new BigDecimal("100.00")),
                Currency.getInstance("GBP")
        );
        Transaction withdrawal = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                new Amount(new BigDecimal("50.00")),
                Currency.getInstance("GBP")

        );

        repo.save(deposit);
        repo.save(withdrawal);
        // Create a transaction for different account
        repo.save(Transaction.create(
                AccountId.newId(),
                TransactionType.DEPOSIT,
                new Amount(new BigDecimal("75.00")),
                Currency.getInstance("GBP")

        ));

        List<Transaction> accountTxns = repo.findByAccount(accountId);
        assertThat(accountTxns).hasSize(2);
        assertThat(accountTxns)
                .extracting(t -> t.getAmount().value())
                .containsExactlyInAnyOrder(
                        new BigDecimal("100.00"),
                        new BigDecimal("50.00")
                );
    }

    @Test
    void shouldPreserveAllTransactionFields() {
        TransactionId id = TransactionId.of(UUID.randomUUID());
        AccountId accountId = AccountId.of(UUID.randomUUID());
        Amount amount = new Amount(new BigDecimal("123.45"));
        Instant timestamp = Instant.now();
        Currency currency = Currency.getInstance("GBP");

        Transaction original = Transaction.rehydrate(
                id,
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                timestamp,
                currency
        );
        Transaction saved = repo.save(original);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getAccountId()).isEqualTo(accountId);
        assertThat(saved.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(saved.getAmount().value()).isEqualByComparingTo(amount.value());
        assertThat(saved.getTimestamp()).isEqualTo(timestamp);

        Optional<Transaction> fetched = repo.findById(id);
        assertThat(fetched).isPresent();
        Transaction fetchedTxn = fetched.get();
        assertThat(fetchedTxn.getId()).isEqualTo(id);
        assertThat(fetchedTxn.getAccountId()).isEqualTo(accountId);
        assertThat(fetchedTxn.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(fetchedTxn.getAmount().value()).isEqualByComparingTo(amount.value());
        assertThat(fetchedTxn.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<Transaction> result = repo.findById(TransactionId.newId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsForAccount() {
        List<Transaction> transactions = repo.findByAccount(AccountId.newId());
        assertThat(transactions).isEmpty();
    }

    @Test
    void shouldHandleMultipleTransactionTypes() {
        AccountId accountId = AccountId.newId();
        Transaction deposit = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                new Amount(new BigDecimal("200.00")),
                Currency.getInstance("GBP")
        );
        Transaction withdrawal = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                new Amount(new BigDecimal("150.00")),
                Currency.getInstance("GBP")
        );

        repo.save(deposit);
        repo.save(withdrawal);

        List<Transaction> transactions = repo.findByAccount(accountId);
        assertThat(transactions)
                .extracting(Transaction::getType)
                .containsExactlyInAnyOrder(
                        TransactionType.DEPOSIT,
                        TransactionType.WITHDRAWAL
                );
    }

    @Test
    void shouldPreserveTransactionOrder() {
        AccountId accountId = AccountId.newId();
        Transaction first = Transaction.create(
                accountId,
                TransactionType.DEPOSIT,
                new Amount(new BigDecimal("100.00")),
                Currency.getInstance("GBP")
        );
        Transaction second = Transaction.create(
                accountId,
                TransactionType.WITHDRAWAL,
                new Amount(new BigDecimal("50.00")),
                Currency.getInstance("GBP")
        );

        // Ensure there's a small delay between transactions
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        repo.save(first);
        repo.save(second);

        List<Transaction> transactions = repo.findByAccount(accountId);
        assertThat(transactions)
                .extracting(Transaction::getTimestamp)
                .isSorted();
    }
}