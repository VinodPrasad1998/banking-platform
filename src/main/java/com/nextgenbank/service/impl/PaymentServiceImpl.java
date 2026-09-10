package com.nextgenbank.service.impl;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;
import com.nextgenbank.dto.TransferRequest;
import com.nextgenbank.dto.TransferResponse;
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
import com.nextgenbank.exception.PaymentNotFoundException;
import com.nextgenbank.repository.AccountRepository;
import com.nextgenbank.repository.BeneficiaryRepository;
import com.nextgenbank.repository.PaymentRepository;
import com.nextgenbank.service.PaymentService;
import com.nextgenbank.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionService transactionService;

    public PaymentServiceImpl(
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
    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        String normalizedKey = idempotencyKey.trim();

        Payment existingPayment =
                paymentRepository
                        .findByIdempotencyKey(normalizedKey)
                        .orElse(null);

        if (existingPayment != null) {

            validateIdempotencyRequest(
                    existingPayment,
                    request
            );

            return mapToResponse(existingPayment);
        }

        Account sourceAccount =
                accountRepository.findById(
                                request.sourceAccountId())
                        .orElseThrow(() ->
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
                                request.beneficiaryId())
                        .orElseThrow(() ->
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
                accountRepository
                        .findByAccountNumber(
                                beneficiary
                                        .getBeneficiaryAccountNumber())
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Destination account not found for beneficiary"
                                ));

        Payment payment = new Payment();

        payment.setPaymentReference(
                generatePaymentReference()
        );

        payment.setIdempotencyKey(normalizedKey);
        payment.setSourceAccount(sourceAccount);
        payment.setBeneficiary(beneficiary);
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment =
                paymentRepository.save(payment);

        TransferResponse transferResponse =
                transactionService.transfer(
                        new TransferRequest(
                                sourceAccount.getAccountId(),
                                destinationAccount.getAccountId(),
                                request.amount()
                        )
                );

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());

        Payment completedPayment =
                paymentRepository.save(payment);

        return mapToResponse(completedPayment);
    }

    private void validateIdempotencyRequest(
            Payment existingPayment,
            PaymentRequest request) {

        boolean sameSourceAccount =
                existingPayment.getSourceAccount()
                        .getAccountId()
                        .equals(request.sourceAccountId());

        boolean sameBeneficiary =
                existingPayment.getBeneficiary()
                        .getBeneficiaryId()
                        .equals(request.beneficiaryId());

        boolean sameAmount =
                existingPayment.getAmount()
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

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment not found with ID: "
                                                + paymentId
                                ));

        return mapToResponse(payment);
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