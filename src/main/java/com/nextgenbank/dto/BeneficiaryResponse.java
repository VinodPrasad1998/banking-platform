package com.nextgenbank.dto;

import com.nextgenbank.entity.BeneficiaryStatus;

import java.time.LocalDateTime;

public record BeneficiaryResponse(

        Long beneficiaryId,
        Long customerId,
        String beneficiaryName,
        String bankName,
        String beneficiaryAccountNumber,
        String ifscCode,
        BeneficiaryStatus status,
        LocalDateTime createdAt
) {
}