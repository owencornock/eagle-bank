package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.account.Account;
import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.account.AccountName;
import com.eaglebank.eaglebankdomain.account.Balance;
import com.eaglebank.eaglebankdomain.exception.ForbiddenException;
import com.eaglebank.eaglebankdomain.exception.ResourceNotFoundException;
import com.eaglebank.eaglebankdomain.user.UserId;
import com.eaglebank.eaglebanklogic.account.AccountService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    private static final String BASE = "/v1/accounts";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_ID_STR = USER_ID.toString();
    private static final UUID ACCT_ID = UUID.randomUUID();
    private static final String ACCT_ID_STR = ACCT_ID.toString();
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AccountController controller;
    @org.mockito.Mock
    private AccountService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // inject our mock into the controller
        ReflectionTestUtils.setField(controller, "service", service);
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(AccountControllerTest.USER_ID_STR, null);
    }

    @Test
    @DisplayName("POST   /v1/accounts    → 201 CREATED")
    void createAccountSuccess() throws Exception {
        AccountName name = new AccountName("My Savings");
        Balance initialBal = new Balance(BigDecimal.ZERO);
        Account acct = Account.rehydrate(
                AccountId.of(ACCT_ID),
                UserId.of(USER_ID),
                name,
                initialBal
        );

        given(service.createAccount(
                eq(UserId.of(USER_ID)),
                eq(name)
        )).willReturn(acct);

        mvc.perform(post(BASE)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"My Savings\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE + "/" + ACCT_ID_STR))
                .andExpect(jsonPath("$.id").value(ACCT_ID_STR))
                .andExpect(jsonPath("$.name").value("My Savings"))
                .andExpect(jsonPath("$.ownerId").value(USER_ID_STR))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("POST   /v1/accounts    → 400 BAD REQUEST (validation)")
    void createAccountValidationFail() throws Exception {
        mvc.perform(post(BASE)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET    /v1/accounts    → 200 OK (list)")
    void listAccountsSuccess() throws Exception {
        Account a1 = Account.rehydrate(
                AccountId.of(UUID.randomUUID()),
                UserId.of(USER_ID),
                new AccountName("A"),
                new Balance(BigDecimal.TEN)
        );
        Account a2 = Account.rehydrate(
                AccountId.of(UUID.randomUUID()),
                UserId.of(USER_ID),
                new AccountName("B"),
                new Balance(BigDecimal.valueOf(5))
        );
        given(service.listAccounts(UserId.of(USER_ID)))
                .willReturn(List.of(a1, a2));

        mvc.perform(get(BASE)
                        .principal(auth())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("A")))
                .andExpect(jsonPath("$[1].balance", is(5.0)));
    }

    @Test
    @DisplayName("GET    /v1/accounts/{id} → 200 OK")
    void fetchAccountSuccess() throws Exception {
        Account acct = Account.rehydrate(
                AccountId.of(ACCT_ID),
                UserId.of(USER_ID),
                new AccountName("X"),
                new Balance(BigDecimal.valueOf(99))
        );
        given(service.fetchAccount(
                eq(AccountId.of(ACCT_ID)),
                eq(UserId.of(USER_ID))
        )).willReturn(acct);

        mvc.perform(get(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCT_ID_STR))
                .andExpect(jsonPath("$.name").value("X"))
                .andExpect(jsonPath("$.ownerId").value(USER_ID_STR))
                .andExpect(jsonPath("$.balance").value(99));
    }

    @Test
    @DisplayName("GET    /v1/accounts/{id} → 403 FORBIDDEN")
    void fetchAccountForbidden() throws Exception {
        willThrow(new ForbiddenException("nope")).given(service)
                .fetchAccount(any(), any());

        mvc.perform(get(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET    /v1/accounts/{id} → 404 NOT FOUND")
    void fetchAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("missing")).given(service)
                .fetchAccount(any(), any());

        mvc.perform(get(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH  /v1/accounts/{id} → 200 OK")
    void updateAccountSuccess() throws Exception {
        Account updated = Account.rehydrate(
                AccountId.of(ACCT_ID),
                UserId.of(USER_ID),
                new AccountName("NewName"),
                new Balance(BigDecimal.ZERO)
        );
        given(service.updateAccount(
                eq(AccountId.of(ACCT_ID)),
                eq(UserId.of(USER_ID)),
                eq(new AccountName("NewName"))
        )).willReturn(updated);

        mvc.perform(patch(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    @DisplayName("PATCH  /v1/accounts/{id} → 403 FORBIDDEN")
    void updateAccountForbidden() throws Exception {
        willThrow(new ForbiddenException("nope")).given(service)
                .updateAccount(any(), any(), any());

        mvc.perform(patch(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH  /v1/accounts/{id} → 404 NOT FOUND")
    void updateAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("missing")).given(service)
                .updateAccount(any(), any(), any());

        mvc.perform(patch(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /v1/accounts/{id} → 204 NO CONTENT")
    void deleteAccountSuccess() throws Exception {
        // default doNothing() on service.deleteAccount()
        mvc.perform(delete(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/accounts/{id} → 403 FORBIDDEN")
    void deleteAccountForbidden() throws Exception {
        willThrow(new ForbiddenException("nope")).given(service)
                .deleteAccount(any(), any());

        mvc.perform(delete(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /v1/accounts/{id} → 404 NOT FOUND")
    void deleteAccountNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("missing")).given(service)
                .deleteAccount(any(), any());

        mvc.perform(delete(BASE + "/" + ACCT_ID_STR)
                        .principal(auth())
                )
                .andExpect(status().isNotFound());
    }
}
