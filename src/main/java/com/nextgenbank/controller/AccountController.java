package com.nextgenbank.controller;


import com.nextgenbank.dto.AccountRequest;
import com.nextgenbank.dto.AccountResponse;
import com.nextgenbank.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.nextgenbank.dto.AccountStatusRequest;

import java.util.Set;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "accountId",
            "accountNumber",
            "accountType",
            "balance",
            "currency",
            "status",
            "createdAt"
    );

    public AccountController(AccountService accountService)
    {
        this.accountService=accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse>   createAccount(@Valid @RequestBody AccountRequest request)
    {
        AccountResponse response=accountService.createAccount(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{accountId}")
    public ResponseEntity <AccountResponse> getAccountById(
            @PathVariable
            @Positive(message ="Account ID must be greater than 0")
            Long accountId)
    {
        return ResponseEntity.ok(
                accountService.getAccountById(accountId)
        );
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size,

            @RequestParam(defaultValue = "accountId")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed values: "
                            + "accountId, accountNumber, accountType, "
                            + "balance, currency, status, createdAt."
            );
        }
        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new IllegalArgumentException(
                    "Invalid sort direction. Allowed values: asc, desc."
            );
        }

        return ResponseEntity.ok(
                accountService.getAllAccounts(
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(

            @PathVariable
            @Positive(message = "Account ID must be greater than 0")
            Long accountId,

            @Valid @RequestBody
            AccountStatusRequest request) {

        AccountResponse response =
                accountService.updateAccountStatus(
                        accountId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    }
