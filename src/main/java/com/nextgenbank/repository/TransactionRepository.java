package com.nextgenbank.repository;

import com.nextgenbank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByTransactionReference(String transactionReference);
    Page<Transaction> findByAccountAccountId( Long accountId,Pageable pageable);


}