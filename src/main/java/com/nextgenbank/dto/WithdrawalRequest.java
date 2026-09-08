package com.nextgenbank.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawalRequest(

        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Withdrawal amount is required")
        @Positive(message = "Withdrawal amount must be greater than 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Withdrawal amount must have maximum 2 decimal places"
        )
        BigDecimal amount
) {
}