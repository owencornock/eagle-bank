package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.transaction.Amount;
import com.eaglebank.eaglebankdomain.transaction.Transaction;
import com.eaglebank.eaglebankdomain.transaction.TransactionId;
import com.eaglebank.eaglebankdomain.transaction.TransactionType;
import com.eaglebank.eaglebanklogic.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    private static final String BASE = "/v1/accounts";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_ID_STR = USER_ID.toString();
    private static final UUID ACCT_ID = UUID.randomUUID();
    private static final String ACCT_ID_STR = ACCT_ID.toString();
    private static final UUID TXN_ID = UUID.randomUUID();
    private static final String TXN_ID_STR = TXN_ID.toString();
    @Autowired
    private MockMvc mvc;
    @Autowired
    private TransactionController controller;
    @org.mockito.Mock
    private TransactionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(controller, "service", service);
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(TransactionControllerTest.USER_ID_STR, null);
    }

    @Test
    @DisplayName("POST /v1/accounts/{accountId}/transactions – deposit succeeds → 201")
    void depositSuccess() throws Exception {
        Transaction txn = Transaction.rehydrate(
                TransactionId.of(TXN_ID),
                AccountId.of(ACCT_ID),
                TransactionType.DEPOSIT,
                new Amount(BigDecimal.valueOf(50)),
                Instant.now(),
                Currency.getInstance("GBP")
        );
        given(service.deposit(
                eq(AccountId.of(ACCT_ID)),
                eq(com.eaglebank.eaglebankdomain.user.UserId.of(USER_ID)),
                eq(new Amount(BigDecimal.valueOf(50)))
        )).willReturn(txn);

        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"DEPOSIT",
                                  "amount":50,
                                  "currency":"GBP"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        BASE + "/" + ACCT_ID_STR + "/transactions/" + TXN_ID_STR))
                .andExpect(jsonPath("$.id").value(TXN_ID_STR))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("POST /…/transactions – withdrawal succeeds → 201")
    void withdrawalSuccess() throws Exception {
        Transaction txn = Transaction.rehydrate(
                TransactionId.of(TXN_ID),
                AccountId.of(ACCT_ID),
                TransactionType.WITHDRAWAL,
                new Amount(BigDecimal.valueOf(20)),
                Instant.now(),
                Currency.getInstance("GBP")
        );
        given(service.withdraw(
                eq(AccountId.of(ACCT_ID)),
                eq(com.eaglebank.eaglebankdomain.user.UserId.of(USER_ID)),
                eq(new Amount(BigDecimal.valueOf(20)))
        )).willReturn(txn);

        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"WITHDRAWAL",
                                  "amount":20,
                                  "currency":"GBP"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(20));
    }

    @Test
    @DisplayName("POST /…/transactions – missing fields → 400")
    void createTransactionValidationFail() throws Exception {
        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ }")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /…/transactions – account not found → 404")
    void createTransactionAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("no acct"))
                .given(service).deposit(any(), any(), any());
        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"DEPOSIT",
                                  "amount":10,
                                  "currency":"GBP"
                                }
                                """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /…/transactions – forbidden (wrong user) → 403")
    void createTransactionForbidden() throws Exception {
        willThrow(new ForbiddenException("nope"))
                .given(service).withdraw(any(), any(), any());
        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"WITHDRAWAL",
                                  "amount":5,
                                  "currency":"GBP"
                                }
                                """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /…/transactions – insufficient funds → 422")
    void withdrawalInsufficientFunds() throws Exception {
        willThrow(new com.eaglebank.eaglebankdomain.exception.InvalidUserDataException("Insufficient"))
                .given(service).withdraw(any(), any(), any());
        mvc.perform(post(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"WITHDRAWAL",
                                  "amount":100,
                                  "currency":"GBP"
                                }
                                """)
                )
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET  /v1/accounts/{accountId}/transactions – success")
    void listTransactionsSuccess() throws Exception {
        Transaction t1 = Transaction.rehydrate(
                TransactionId.of(UUID.randomUUID()),
                AccountId.of(ACCT_ID),
                TransactionType.DEPOSIT,
                new Amount(BigDecimal.TEN),
                Instant.now(),
                Currency.getInstance("GBP")
        );
        Transaction t2 = Transaction.rehydrate(
                TransactionId.of(UUID.randomUUID()),
                AccountId.of(ACCT_ID),
                TransactionType.WITHDRAWAL,
                new Amount(BigDecimal.valueOf(3)),
                Instant.now(),
                Currency.getInstance("GBP")
        );
        given(service.listTransactions(
                eq(AccountId.of(ACCT_ID)),
                eq(com.eaglebank.eaglebankdomain.user.UserId.of(USER_ID))
        )).willReturn(List.of(t1, t2));

        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].amount").value(3));
    }

    @Test
    @DisplayName("GET  /…/transactions – forbidden → 403")
    void listTransactionsForbidden() throws Exception {
        willThrow(new ForbiddenException("nope"))
                .given(service).listTransactions(any(), any());
        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET  /…/transactions – account missing → 404")
    void listTransactionsAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("no acct"))
                .given(service).listTransactions(any(), any());
        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions")
                        .principal(auth())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET  /v1/accounts/{accountId}/transactions/{txnId} – success")
    void fetchTransactionSuccess() throws Exception {
        Transaction txn = Transaction.rehydrate(
                TransactionId.of(TXN_ID),
                AccountId.of(ACCT_ID),
                TransactionType.DEPOSIT,
                new Amount(BigDecimal.ONE),
                Instant.now(),
                Currency.getInstance("GBP")
        );
        given(service.fetchTransaction(
                eq(AccountId.of(ACCT_ID)),
                eq(TransactionId.of(TXN_ID)),
                eq(com.eaglebank.eaglebankdomain.user.UserId.of(USER_ID))
        )).willReturn(txn);

        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions/" + TXN_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TXN_ID_STR))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(1));
    }

    @Test
    @DisplayName("GET  /…/transactions/{txnId} – forbidden → 403")
    void fetchTransactionForbidden() throws Exception {
        willThrow(new ForbiddenException("nope"))
                .given(service).fetchTransaction(any(), any(), any());
        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions/" + TXN_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET  /…/transactions/{txnId} – not found (account)")
    void fetchTransactionAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("no acct"))
                .given(service).fetchTransaction(any(), any(), any());
        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions/" + TXN_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET  /…/transactions/{txnId} – not found (txn)")
    void fetchTransactionNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("no txn"))
                .given(service).fetchTransaction(any(), any(), any());
        mvc.perform(get(BASE + "/" + ACCT_ID_STR + "/transactions/" + TXN_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isNotFound());
    }
}
