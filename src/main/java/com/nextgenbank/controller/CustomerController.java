package com.nextgenbank.controller;

import com.nextgenbank.dto.CustomerRequest;
import com.nextgenbank.dto.CustomerResponse;
import com.nextgenbank.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/customers")
@Validated
public class CustomerController {
    private final CustomerService customerService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "customerId",
            "firstName",
            "lastName",
            "email"
    );
    public CustomerController(CustomerService customerService)
    {

        this.customerService=customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest)
    {
        CustomerResponse response =
                customerService.registerCustomer(customerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long customerId) {

        CustomerResponse response =
                customerService.getCustomerById(customerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(

            @RequestParam(defaultValue = "0")@Min(0)int page,
            @RequestParam(defaultValue = "10")@Min(1)@Max(100)int size,
            @RequestParam(defaultValue = "customerId") String sortBy,
            @RequestParam(defaultValue = "asc")String direction)
    {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed values: customerId, firstName, lastName, email."
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new IllegalArgumentException(
                    "Invalid sort direction. Allowed values: asc, desc."
            );
        }

        return ResponseEntity.ok(
                customerService.getAllCustomers(page,size,sortBy,direction));

    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Map<String ,String>>deleteCustomer(
            @PathVariable  Long customerId)
    {
        customerService.deleteCustomer(customerId);

        Map<String ,String> response=new HashMap<>();
        response.put("message","Customer with id" + customerId +   "deleted Successfully");
        return ResponseEntity.ok(response);


    }



    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest customerRequest) {

        CustomerResponse response =
                customerService.updateCustomer(customerId, customerRequest);

        return ResponseEntity.ok(response);
    }

}
