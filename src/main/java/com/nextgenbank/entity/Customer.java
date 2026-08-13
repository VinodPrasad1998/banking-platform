package com.nextgenbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    public Customer() {

    }
    @NotBlank(message = "First Name is required")
    @Size(max = 50, message = "First Name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message="Last Name is Required ")
    @Size(max=50,message = "Last Name cannot exceed 50 Characters")
    private String lastName;

    @NotBlank(message = "Mobile Number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message ="Mobile Number Must Contain Exactly 10 digits")
    private String mobileNumber;

    @NotNull(message = "Date of birth is required ")
    private LocalDate dob;

    @NotNull(message = "City is Required ")
    private String city;

    @NotBlank(message ="Address is required")
    private String address;

    @NotBlank(message = "State is Required")
    private String state;

    @NotNull(message = "Country is Required")
    private String country;

    @NotBlank(message = "PinCode is Required")
    private String pinCode;

    @NotBlank(message = "Email is Required")
    @Email(message = "Enter a Valid email")
    @Column(unique = true,nullable = false)
    private String email;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}