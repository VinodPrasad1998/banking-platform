package com.nextgenbank.controller;

import com.nextgenbank.entity.Customer;
import com.nextgenbank.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService)
    {

        this.customerService=customerService;
    }

    @PostMapping
    public Customer createCustomer(@Valid @RequestBody Customer customer)
    {
        return customerService.registerCustomer(customer);
    }

}
