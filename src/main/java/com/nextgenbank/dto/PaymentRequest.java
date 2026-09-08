package com.nextgenbank.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(message = "Source account ID is required")
        Long sourceAccountId,

        @NotNull(message = "Beneficiary ID is required")
        Long beneficiaryId,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Payment amount must have maximum 2 decimal places"
        )
        BigDecimal amount
) {
}