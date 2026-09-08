package com.nextgenbank.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(

        @NotNull(message = "Source account ID is required")
        Long sourceAccountId,

        @NotNull(message = "Destination account ID is required")
        Long destinationAccountId,

        @NotNull(message = "Transfer amount is required")
        @Positive(message = "Transfer amount must be greater than 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Transfer amount must have maximum 2 decimal places"
        )
        BigDecimal amount
) {
}