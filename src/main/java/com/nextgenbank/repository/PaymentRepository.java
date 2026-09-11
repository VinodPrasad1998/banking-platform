package com.nextgenbank.repository;

import com.nextgenbank.entity.Payment;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(
            String idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Payment p
            join fetch p.sourceAccount
            join fetch p.beneficiary
            where p.idempotencyKey = :idempotencyKey
            """)
    Optional<Payment> findByIdempotencyKeyForUpdate(
            @Param("idempotencyKey")
            String idempotencyKey
    );

    boolean existsByPaymentReference(
            String paymentReference
    );
}