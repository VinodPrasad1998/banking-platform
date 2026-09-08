package com.nextgenbank.dto;

import com.nextgenbank.entity.TransactionStatus;
import com.nextgenbank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long transactionId,

        String transactionReference,

        String transferReference,

        Long accountId,

        String accountNumber,

        TransactionType transactionType,

        BigDecimal amount,

        BigDecimal balanceAfter,

        String currency,

        TransactionStatus status,

        LocalDateTime createdAt

) {
}