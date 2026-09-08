package com.nextgenbank.dto;

import com.nextgenbank.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long paymentId,
        String paymentReference,
        String idempotencyKey,

        Long sourceAccountId,
        String sourceAccountNumber,

        Long beneficiaryId,
        String beneficiaryName,
        String beneficiaryAccountNumber,

        BigDecimal amount,

        PaymentStatus status,

        LocalDateTime createdAt,
        LocalDateTime completedAt,

        String failureReason
) {
}