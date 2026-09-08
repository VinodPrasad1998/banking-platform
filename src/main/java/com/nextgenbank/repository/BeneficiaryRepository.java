package com.nextgenbank.repository;

import com.nextgenbank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerCustomerId(Long customerId);
}