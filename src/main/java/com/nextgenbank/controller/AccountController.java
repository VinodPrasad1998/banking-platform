package com.nextgenbank.controller;


import com.nextgenbank.dto.AccountRequest;
import com.nextgenbank.dto.AccountResponse;
import com.nextgenbank.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;

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

}
