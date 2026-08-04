package com.nextgenbank.service.impl;

import com.nextgenbank.entity.Customer;
import com.nextgenbank.repository.CustomerRepository;
import com.nextgenbank.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository)
    {
        this.customerRepository=customerRepository;
    }

    @Override
    public Customer registerCustomer(Customer customer)
    {
        return customerRepository.save(customer);
    }

}
