package com.nextgenbank.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter


public class CustomerResponse {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private LocalDate dob;
    private String city;
    private String address;
    private String state;
    private String country;
    private String pinCode;


}
