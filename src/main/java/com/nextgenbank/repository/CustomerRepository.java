package com.nextgenbank.repository;

import com.nextgenbank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  CustomerRepository extends JpaRepository <Customer,Long>
        {

                boolean existsByEmail(String email);

                boolean existsByEmailAndCustomerIdNot(String email,Long customerId);

        }

