package com.nextgenbank.exception;

public class PaymentConcurrencyException extends RuntimeException {

    public PaymentConcurrencyException(String message) {
        super(message);
    }
}