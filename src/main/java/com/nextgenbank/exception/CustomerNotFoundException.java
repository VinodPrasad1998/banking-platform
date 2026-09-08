package com.nextgenbank.exception;

import com.nextgenbank.entity.Customer;

public class CustomerNotFoundException extends RuntimeException{

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
