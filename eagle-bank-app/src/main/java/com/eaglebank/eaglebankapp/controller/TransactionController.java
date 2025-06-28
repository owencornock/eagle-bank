package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankdomain.transaction.*;
import com.eaglebank.eaglebankdomain.account.AccountId;
import com.eaglebank.eaglebankdomain.user.UserId;
import com.eaglebank.eaglebanklogic.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts/{accountId}/transactions")
@Validated
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    public record CreateTransactionRequest(
            @NotNull TransactionType type,
            @NotNull @DecimalMin("0.00") BigDecimal amount
    ) {}

    public record TransactionResponse(
            String id,
            TransactionType type,
            BigDecimal amount,
            String timestamp
    ) {}

    @Operation(summary = "Create a new transaction (deposit or withdrawal)")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody CreateTransactionRequest req,
            Authentication auth
    ) {
        UserId user = UserId.of(UUID.fromString(auth.getName()));
        AccountId acctId = AccountId.of(UUID.fromString(accountId));
        Transaction txn;
        if (req.type() == TransactionType.DEPOSIT) {
            txn = service.deposit(acctId, user, new Amount(req.amount()));
        } else {
            txn = service.withdraw(acctId, user, new Amount(req.amount()));
        }
        TransactionResponse resp = toResponse(txn);
        return ResponseEntity.created(URI.create(
                "/v1/accounts/" + accountId + "/transactions/" + resp.id()
        )).body(resp);
    }

    @Operation(summary = "List all transactions for an account")
    @GetMapping
    public List<TransactionResponse> listTransactions(
            @PathVariable String accountId,
            Authentication auth
    ) {
        UserId user = UserId.of(UUID.fromString(auth.getName()));
        AccountId acctId = AccountId.of(UUID.fromString(accountId));
        return service.listTransactions(acctId, user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Fetch a specific transaction by ID")
    @GetMapping("/{transactionId}")
    public TransactionResponse fetchTransaction(
            @PathVariable String accountId,
            @PathVariable String transactionId,
            Authentication auth
    ) {
        UserId user = UserId.of(UUID.fromString(auth.getName()));
        AccountId acctId = AccountId.of(UUID.fromString(accountId));
        Transaction txn = service.fetchTransaction(
                acctId,
                TransactionId.of(UUID.fromString(transactionId)),
                user
        );
        return toResponse(txn);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId().value().toString(),
                t.getType(),
                t.getAmount().value(),
                t.getTimestamp().toString()
        );
    }
}
