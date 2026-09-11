package com.nextgenbank.service.impl;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;
import com.nextgenbank.dto.TransferRequest;
import com.nextgenbank.entity.Account;
import com.nextgenbank.entity.AccountStatus;
import com.nextgenbank.entity.Beneficiary;
import com.nextgenbank.entity.BeneficiaryStatus;
import com.nextgenbank.entity.Payment;
import com.nextgenbank.entity.PaymentStatus;
import com.nextgenbank.exception.AccountNotActiveException;
import com.nextgenbank.exception.AccountNotFoundException;
import com.nextgenbank.exception.BeneficiaryNotActiveException;
import com.nextgenbank.exception.BeneficiaryNotFoundException;
import com.nextgenbank.exception.IdempotencyKeyConflictException;
import com.nextgenbank.exception.PaymentConcurrencyException;
import com.nextgenbank.repository.AccountRepository;
import com.nextgenbank.repository.BeneficiaryRepository;
import com.nextgenbank.repository.PaymentRepository;
import com.nextgenbank.service.PaymentIdempotencyService;
import com.nextgenbank.service.TransactionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentIdempotencyServiceImpl
        implements PaymentIdempotencyService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionService transactionService;

    public PaymentIdempotencyServiceImpl(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            BeneficiaryRepository beneficiaryRepository,
            TransactionService transactionService) {

        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionService = transactionService;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findExistingPayment(
            PaymentRequest request,
            String idempotencyKey) {

        return paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .map(payment -> {

                    validateIdempotencyRequest(
                            payment,
                            request
                    );

                    return mapToResponse(payment);
                })
                .orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse executePayment(
            PaymentRequest request,
            String idempotencyKey) {

        Account sourceAccount =
                accountRepository.findById(
                        request.sourceAccountId()
                ).orElseThrow(() ->
                        new AccountNotFoundException(
                                "Source account not found with ID: "
                                        + request.sourceAccountId()
                        ));

        if (sourceAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new AccountNotActiveException(
                    "Payment is not allowed because source account status is "
                            + sourceAccount.getStatus()
            );
        }

        Beneficiary beneficiary =
                beneficiaryRepository.findById(
                        request.beneficiaryId()
                ).orElseThrow(() ->
                        new BeneficiaryNotFoundException(
                                "Beneficiary not found with ID: "
                                        + request.beneficiaryId()
                        ));

        if (beneficiary.getStatus()
                != BeneficiaryStatus.ACTIVE) {

            throw new BeneficiaryNotActiveException(
                    "Payment is not allowed because beneficiary status is "
                            + beneficiary.getStatus()
            );
        }

        Account destinationAccount =
                accountRepository.findByAccountNumber(
                        beneficiary.getBeneficiaryAccountNumber()
                ).orElseThrow(() ->
                        new AccountNotFoundException(
                                "Destination account not found for beneficiary"
                        ));

        Payment payment = new Payment();

        payment.setPaymentReference(
                generatePaymentReference()
        );

        payment.setIdempotencyKey(
                idempotencyKey
        );

        payment.setSourceAccount(
                sourceAccount
        );

        payment.setBeneficiary(
                beneficiary
        );

        payment.setAmount(
                request.amount()
        );

        payment.setStatus(
                PaymentStatus.PROCESSING
        );

        payment.setCreatedAt(
                LocalDateTime.now()
        );

        /*
         * IMPORTANT:
         *
         * This flush happens INSIDE this transaction.
         *
         * If another request already inserted the same
         * idempotency key, this transaction fails here.
         *
         * The entire transaction then rolls back.
         *
         * No stale Payment entity escapes this transaction.
         */
        try {

            paymentRepository.saveAndFlush(payment);

        } catch (DataIntegrityViolationException ex) {

            throw new PaymentConcurrencyException(
                    "Another request is already processing "
                            + "this idempotency key."
            );
        }

        /*
         * Payment creation and money movement are now
         * part of the SAME transaction.
         */
        transactionService.transfer(
                new TransferRequest(
                        sourceAccount.getAccountId(),
                        destinationAccount.getAccountId(),
                        request.amount()
                )
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setCompletedAt(
                LocalDateTime.now()
        );

        /*
         * payment is already managed by the current
         * persistence context.
         *
         * No separate save is required.
         */
        return mapToResponse(payment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse resolveConcurrentPayment(
            PaymentRequest request,
            String idempotencyKey) {

        Payment payment =
                paymentRepository
                        .findByIdempotencyKeyForUpdate(
                                idempotencyKey
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Concurrent payment could not be found."
                                ));

        validateIdempotencyRequest(
                payment,
                request
        );

        /*
         * Build the DTO while the transaction/session
         * is still alive.
         *
         * Nothing JPA-related escapes this method.
         */
        return mapToResponse(payment);
    }

    private void validateIdempotencyRequest(
            Payment payment,
            PaymentRequest request) {

        boolean sameSourceAccount =
                payment.getSourceAccount()
                        .getAccountId()
                        .equals(request.sourceAccountId());

        boolean sameBeneficiary =
                payment.getBeneficiary()
                        .getBeneficiaryId()
                        .equals(request.beneficiaryId());

        boolean sameAmount =
                payment.getAmount()
                        .compareTo(request.amount()) == 0;

        if (!sameSourceAccount
                || !sameBeneficiary
                || !sameAmount) {

            throw new IdempotencyKeyConflictException(
                    "Idempotency key has already been used "
                            + "with a different payment request."
            );
        }
    }

    private String generatePaymentReference() {

        return "PAY-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

    private PaymentResponse mapToResponse(
            Payment payment) {

        Account sourceAccount =
                payment.getSourceAccount();

        Beneficiary beneficiary =
                payment.getBeneficiary();

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getPaymentReference(),
                payment.getIdempotencyKey(),
                sourceAccount.getAccountId(),
                sourceAccount.getAccountNumber(),
                beneficiary.getBeneficiaryId(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBeneficiaryAccountNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getCompletedAt(),
                payment.getFailureReason()
        );
    }
}