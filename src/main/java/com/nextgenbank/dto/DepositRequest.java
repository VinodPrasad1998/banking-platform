package com.nextgenbank.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(

        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Deposit amount is required")
        @Positive(message = "Deposit amount must be greater than 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Deposit amount must have maximum 2 decimal places"
        )
        BigDecimal amount

) {
}