package com.nextgenbank.controller;

import com.nextgenbank.dto.DepositRequest;
import com.nextgenbank.dto.TransactionResponse;
import com.nextgenbank.service.TransactionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nextgenbank.dto.WithdrawalRequest;
import com.nextgenbank.dto.TransferRequest;
import com.nextgenbank.dto.TransferResponse;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request) {

        TransactionResponse response =
                transactionService.deposit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawalRequest request) {

        TransactionResponse response =
                transactionService.withdraw(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response =
                transactionService.transfer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long transactionId) {

        TransactionResponse response =
                transactionService.getTransactionById(transactionId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable Long accountId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Page<TransactionResponse> response =
                transactionService.getTransactionHistory(
                        accountId,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(response);
    }
}