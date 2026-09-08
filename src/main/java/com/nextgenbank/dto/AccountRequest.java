package com.nextgenbank.dto;

import com.nextgenbank.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(
    @NotNull(message = "Account type is required")
    AccountType accountType,
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be a valid 3-letter currency code"
    )

    String currency ,
    @NotNull(message = "customer ID is required")
    Long customerId
)
{

        }
