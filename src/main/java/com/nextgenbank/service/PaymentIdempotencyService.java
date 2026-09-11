package com.nextgenbank.service;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;

public interface PaymentIdempotencyService {

    PaymentResponse findExistingPayment(
            PaymentRequest request,
            String idempotencyKey
    );

    PaymentResponse executePayment(
            PaymentRequest request,
            String idempotencyKey
    );

    PaymentResponse resolveConcurrentPayment(
            PaymentRequest request,
            String idempotencyKey
    );
}