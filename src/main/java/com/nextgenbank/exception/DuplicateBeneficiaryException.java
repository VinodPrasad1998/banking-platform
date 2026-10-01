package com.nextgenbank.exception;

public class DuplicateBeneficiaryException extends RuntimeException {
  public DuplicateBeneficiaryException(String message) {
    super(message);
  }
}
