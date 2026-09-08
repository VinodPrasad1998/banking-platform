package com.nextgenbank.dto;

public record TransferResponse(
        String transferReference,
        TransactionResponse sourceTransaction,
        TransactionResponse destinationTransaction
) {
}