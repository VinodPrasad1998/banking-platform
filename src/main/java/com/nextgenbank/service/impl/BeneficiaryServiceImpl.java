package com.nextgenbank.service.impl;

import com.nextgenbank.dto.BeneficiaryRequest;
import com.nextgenbank.dto.BeneficiaryResponse;
import com.nextgenbank.dto.BeneficiaryStatusRequest;
import com.nextgenbank.entity.Beneficiary;
import com.nextgenbank.entity.BeneficiaryStatus;
import com.nextgenbank.entity.Customer;
import com.nextgenbank.exception.BeneficiaryNotFoundException;
import com.nextgenbank.exception.CustomerNotFoundException;
import com.nextgenbank.repository.BeneficiaryRepository;
import com.nextgenbank.repository.CustomerRepository;
import com.nextgenbank.service.BeneficiaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;

    public BeneficiaryServiceImpl(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public BeneficiaryResponse createBeneficiary(
            BeneficiaryRequest request) {

        Customer customer = customerRepository.findById(
                        request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with ID: "
                                + request.customerId()
                ));

        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setCustomer(customer);
        beneficiary.setBeneficiaryName(
                request.beneficiaryName().trim()
        );
        beneficiary.setBankName(
                request.bankName().trim()
        );
        beneficiary.setBeneficiaryAccountNumber(
                request.beneficiaryAccountNumber().trim()
        );
        beneficiary.setIfscCode(
                request.ifscCode().trim().toUpperCase()
        );

        // Newly registered beneficiaries require activation.
        beneficiary.setStatus(BeneficiaryStatus.PENDING);

        beneficiary.setCreatedAt(LocalDateTime.now());

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        return mapToResponse(savedBeneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(
            Long beneficiaryId) {

        Beneficiary beneficiary =
                beneficiaryRepository.findById(beneficiaryId)
                        .orElseThrow(() ->
                                new BeneficiaryNotFoundException(
                                        "Beneficiary not found with ID: "
                                                + beneficiaryId
                                ));

        return mapToResponse(beneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByCustomer(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        }

        return beneficiaryRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiaryStatus(
            Long beneficiaryId,
            BeneficiaryStatusRequest request) {

        Beneficiary beneficiary =
                beneficiaryRepository.findById(beneficiaryId)
                        .orElseThrow(() ->
                                new BeneficiaryNotFoundException(
                                        "Beneficiary not found with ID: "
                                                + beneficiaryId
                                ));

        BeneficiaryStatus currentStatus =
                beneficiary.getStatus();

        BeneficiaryStatus requestedStatus =
                request.status();

        validateStatusTransition(
                currentStatus,
                requestedStatus
        );

        beneficiary.setStatus(requestedStatus);

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        return mapToResponse(savedBeneficiary);
    }

    private void validateStatusTransition(
            BeneficiaryStatus currentStatus,
            BeneficiaryStatus requestedStatus) {

        boolean validTransition = switch (currentStatus) {

            case PENDING ->
                    requestedStatus == BeneficiaryStatus.ACTIVE
                            || requestedStatus == BeneficiaryStatus.BLOCKED;

            case ACTIVE ->
                    requestedStatus == BeneficiaryStatus.BLOCKED;

            case BLOCKED ->
                    requestedStatus == BeneficiaryStatus.ACTIVE;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid beneficiary status transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }
    }

    private BeneficiaryResponse mapToResponse(
            Beneficiary beneficiary) {

        return new BeneficiaryResponse(
                beneficiary.getBeneficiaryId(),
                beneficiary.getCustomer().getCustomerId(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBankName(),
                beneficiary.getBeneficiaryAccountNumber(),
                beneficiary.getIfscCode(),
                beneficiary.getStatus(),
                beneficiary.getCreatedAt()
        );
    }
}