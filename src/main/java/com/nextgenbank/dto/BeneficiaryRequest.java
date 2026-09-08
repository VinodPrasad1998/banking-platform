package com.nextgenbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotBlank(message = "Beneficiary name is required")
        @Size(max = 100, message = "Beneficiary name cannot exceed 100 characters")
        String beneficiaryName,

        @NotBlank(message = "Bank name is required")
        @Size(max = 100, message = "Bank name cannot exceed 100 characters")
        String bankName,

        @NotBlank(message = "Beneficiary account number is required")
        @Size(max = 30, message = "Beneficiary account number cannot exceed 30 characters")
        String beneficiaryAccountNumber,

        @NotBlank(message = "IFSC code is required")
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "Invalid IFSC code"
        )
        String ifscCode
) {
}