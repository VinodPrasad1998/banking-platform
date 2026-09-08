package com.nextgenbank.dto;

import com.nextgenbank.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusRequest(

        @NotNull(message = "Account status is required")
        AccountStatus status

) {
}