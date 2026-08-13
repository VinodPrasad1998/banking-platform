package com.nextgenbank.dto;

import com.nextgenbank.entity.AccountStatus;
import com.nextgenbank.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(

        Long accountId,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        LocalDateTime createdAt,
        Long customerId

) {
}
