package com.nextgenbank.service;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(
            PaymentRequest request,
            String idempotencyKey
    );

    PaymentResponse getPaymentById(
            Long paymentId
    );
}