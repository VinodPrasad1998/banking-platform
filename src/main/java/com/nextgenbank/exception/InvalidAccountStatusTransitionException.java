package com.nextgenbank.exception;

public class InvalidAccountStatusTransitionException extends RuntimeException {
    public InvalidAccountStatusTransitionException(String message) {
        super(message);
    }
}
