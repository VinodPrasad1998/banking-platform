package com.nextgenbank.service;

import com.nextgenbank.dto.BeneficiaryRequest;
import com.nextgenbank.dto.BeneficiaryResponse;
import com.nextgenbank.dto.BeneficiaryStatusRequest;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryResponse createBeneficiary(
            BeneficiaryRequest request
    );

    BeneficiaryResponse getBeneficiaryById(
            Long beneficiaryId
    );

    List<BeneficiaryResponse> getBeneficiariesByCustomer(
            Long customerId
    );

    BeneficiaryResponse updateBeneficiaryStatus(
            Long beneficiaryId,
            BeneficiaryStatusRequest request
    );
}