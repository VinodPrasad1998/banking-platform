package com.nextgenbank.service.impl;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;
import com.nextgenbank.entity.Payment;
import com.nextgenbank.exception.PaymentConcurrencyException;
import com.nextgenbank.exception.PaymentNotFoundException;
import com.nextgenbank.repository.PaymentRepository;
import com.nextgenbank.service.PaymentIdempotencyService;
import com.nextgenbank.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentIdempotencyService paymentIdempotencyService;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentIdempotencyService paymentIdempotencyService) {

        this.paymentRepository = paymentRepository;
        this.paymentIdempotencyService =
                paymentIdempotencyService;
    }

    @Override
    public PaymentResponse createPayment(
            PaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        String normalizedKey =
                idempotencyKey.trim();

        /*
         * IMPORTANT:
         *
         * This method intentionally has NO @Transactional.
         *
         * Every operation below gets its own transaction
         * through PaymentIdempotencyServiceImpl.
         */

        PaymentResponse existingPayment =
                paymentIdempotencyService.findExistingPayment(
                        request,
                        normalizedKey
                );

        if (existingPayment != null) {

            return existingPayment;
        }

        try {

            /*
             * One request wins the database race here.
             *
             * The complete payment + transfer is atomic.
             */
            return paymentIdempotencyService.executePayment(
                    request,
                    normalizedKey
            );

        } catch (PaymentConcurrencyException ex) {

            /*
             * Another request inserted the same
             * idempotency key first.
             *
             * Its transaction has completed/committed
             * before the duplicate insert is reported.
             *
             * Read the committed winner in a NEW transaction.
             */
            return paymentIdempotencyService
                    .resolveConcurrentPayment(
                            request,
                            normalizedKey
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

    private PaymentResponse mapToResponse(
            Payment payment) {

        var sourceAccount =
                payment.getSourceAccount();

        var beneficiary =
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