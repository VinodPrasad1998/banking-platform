package com.nextgenbank.exception;

public class BeneficiaryNotActiveException extends RuntimeException {

    public BeneficiaryNotActiveException(String message) {
        super(message);
    }
}