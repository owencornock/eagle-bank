package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.account.*;
import com.eaglebank.eaglebankdomain.user.UserId;
import com.eaglebank.eaglebanklogic.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts")
@Validated
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    public record CreateAccountRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull AccountType type,
            @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid 3-letter ISO code") String currency
    ) {}

    public record UpdateAccountRequest(
            @NotBlank @Size(max = 100) String name
    ) {}

    public record AccountResponse(
            String id,
            String name,
            String ownerId,
            BigDecimal balance,
            String accountNumber,
            String sortCode,
            AccountType type,
            String currency,
            Instant createdTimestamp,
            Instant updatedTimestamp
    ) {}

    @Operation(summary = "Create a new bank account")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest req,
            Authentication auth
    ) {
        UserId owner = UserId.of(UUID.fromString(auth.getName()));
        Account created = service.createAccount(
                owner,
                new AccountName(req.name()),
                req.type()
        );
        AccountResponse resp = toResponse(created);
        return ResponseEntity
                .created(URI.create("/v1/accounts/" + resp.id()))
                .body(resp);
    }

    @Operation(summary = "List all accounts for the current user")
    @GetMapping
    public List<AccountResponse> listAccounts(Authentication auth) {
        UserId owner = UserId.of(UUID.fromString(auth.getName()));
        return service.listAccounts(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Fetch a single account by ID")
    @GetMapping("/{accountId}")
    public AccountResponse fetchAccount(
            @PathVariable String accountId,
            Authentication auth
    ) {
        UserId owner = UserId.of(UUID.fromString(auth.getName()));
        Account acct = service.fetchAccount(
                AccountId.of(UUID.fromString(accountId)),
                owner
        );
        return toResponse(acct);
    }

    @Operation(summary = "Update an existing account's name")
    @PatchMapping("/{accountId}")
    public AccountResponse updateAccount(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountRequest req,
            Authentication auth
    ) {
        UserId owner = UserId.of(UUID.fromString(auth.getName()));
        Account updated = service.updateAccount(
                AccountId.of(UUID.fromString(accountId)),
                owner,
                new AccountName(req.name())
        );
        return toResponse(updated);
    }

    @Operation(summary = "Delete an account by ID")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable String accountId,
            Authentication auth
    ) {
        UserId owner = UserId.of(UUID.fromString(auth.getName()));
        service.deleteAccount(
                AccountId.of(UUID.fromString(accountId)),
                owner
        );
        return ResponseEntity.noContent().build();
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getId().value().toString(),
                a.getName().value(),
                a.getOwnerId().value().toString(),
                a.getBalance().value(),
                a.getAccountNumber().value(),
                a.getSortCode().value(),
                a.getType(),
                a.getCurrency().getCurrencyCode(),
                a.getCreatedTimestamp(),
                a.getUpdatedTimestamp()
        );
    }
}