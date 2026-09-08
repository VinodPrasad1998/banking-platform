package com.nextgenbank.service;

import com.nextgenbank.dto.CustomerRequest;
import com.nextgenbank.dto.CustomerResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRequest customerRequest);

    Page<CustomerResponse> getAllCustomers(int page,int size,String sortBy,String direction);

    CustomerResponse getCustomerById(Long customerId);

    CustomerResponse updateCustomer(Long customerId, CustomerRequest customerRequest);

    void deleteCustomer(Long customerId);


}
