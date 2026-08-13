package com.nextgenbank.service.impl;

import com.nextgenbank.dto.CustomerRequest;
import com.nextgenbank.dto.CustomerResponse;
import com.nextgenbank.entity.Customer;
import com.nextgenbank.exception.CustomerAlreadyExistsException;
import com.nextgenbank.repository.CustomerRepository;
import com.nextgenbank.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.nextgenbank.exception.CustomerNotFoundException;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponse registerCustomer(CustomerRequest customerRequest) {

        if (customerRepository.existsByEmail(customerRequest.getEmail())) {
            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email: "
                            + customerRequest.getEmail()
            );
        }

        Customer customer = new Customer();

        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setMobileNumber(customerRequest.getMobileNumber());
        customer.setDob(customerRequest.getDob());
        customer.setCity(customerRequest.getCity());
        customer.setAddress(customerRequest.getAddress());
        customer.setState(customerRequest.getState());
        customer.setCountry(customerRequest.getCountry());
        customer.setPinCode(customerRequest.getPinCode());

        Customer savedCustomer = customerRepository.save(customer);

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(savedCustomer.getCustomerId());
        response.setFirstName(savedCustomer.getFirstName());
        response.setLastName(savedCustomer.getLastName());
        response.setEmail(savedCustomer.getEmail());
        response.setMobileNumber(savedCustomer.getMobileNumber());
        response.setDob(savedCustomer.getDob());
        response.setCity(savedCustomer.getCity());
        response.setAddress(savedCustomer.getAddress());
        response.setState(savedCustomer.getState());
        response.setCountry(savedCustomer.getCountry());
        response.setPinCode(savedCustomer.getPinCode());

        return response;
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId));

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(customer.getCustomerId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setMobileNumber(customer.getMobileNumber());
        response.setDob(customer.getDob());
        response.setCity(customer.getCity());
        response.setAddress(customer.getAddress());
        response.setState(customer.getState());
        response.setCountry(customer.getCountry());
        response.setPinCode(customer.getPinCode());

        return response;
    }

    @Override
    public Page<CustomerResponse> getAllCustomers(int page, int size,String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                    ?Sort.by(sortBy).descending()
                    :Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size,sort);

        Page<Customer> customerPage =
                customerRepository.findAll(pageable);

        return customerPage.map(customer -> {

                    CustomerResponse response = new CustomerResponse();

                    response.setCustomerId(customer.getCustomerId());
                    response.setFirstName(customer.getFirstName());
                    response.setLastName(customer.getLastName());
                    response.setEmail(customer.getEmail());
                    response.setMobileNumber(customer.getMobileNumber());
                    response.setDob(customer.getDob());
                    response.setCity(customer.getCity());
                    response.setAddress(customer.getAddress());
                    response.setState(customer.getState());
                    response.setCountry(customer.getCountry());
                    response.setPinCode(customer.getPinCode());

                    return response;
                });
    }

    @Override
    public void deleteCustomer(Long customerId)
    {
        Customer customer=customerRepository.findById(customerId).orElseThrow(()->
                new CustomerNotFoundException("Customer not found with id:"));
        customerRepository.delete(customer);
    }

    @Override
    public CustomerResponse updateCustomer(
            Long customerId,
            CustomerRequest customerRequest) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId
                        ));


        if (customerRepository.existsByEmailAndCustomerIdNot(
                customerRequest.getEmail(),
                customerId)) {

            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email: "
                            + customerRequest.getEmail()
            );
        }

        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setMobileNumber(customerRequest.getMobileNumber());
        customer.setDob(customerRequest.getDob());
        customer.setCity(customerRequest.getCity());
        customer.setAddress(customerRequest.getAddress());
        customer.setState(customerRequest.getState());
        customer.setCountry(customerRequest.getCountry());
        customer.setPinCode(customerRequest.getPinCode());

        Customer updatedCustomer = customerRepository.save(customer);

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(updatedCustomer.getCustomerId());
        response.setFirstName(updatedCustomer.getFirstName());
        response.setLastName(updatedCustomer.getLastName());
        response.setEmail(updatedCustomer.getEmail());
        response.setMobileNumber(updatedCustomer.getMobileNumber());
        response.setDob(updatedCustomer.getDob());
        response.setCity(updatedCustomer.getCity());
        response.setAddress(updatedCustomer.getAddress());
        response.setState(updatedCustomer.getState());
        response.setCountry(updatedCustomer.getCountry());
        response.setPinCode(updatedCustomer.getPinCode());

        return response;
    }

}
