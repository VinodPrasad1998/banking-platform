package com.nextgenbank.dto;

import com.nextgenbank.entity.BeneficiaryStatus;
import jakarta.validation.constraints.NotNull;

public record BeneficiaryStatusRequest(

        @NotNull(message = "Beneficiary status is required")
        BeneficiaryStatus status
) {
}