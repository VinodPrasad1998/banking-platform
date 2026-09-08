package com.nextgenbank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.nextgenbank.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transaction_account", columnList = "account_id"),
                @Index(name = "idx_transaction_reference", columnList = "transaction_reference"),
                @Index(name = "idx_transaction_transfer_reference", columnList = "transfer_reference")
        }
)
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 30
    )
    private String transactionReference;


    @Column(name = "transfer_reference", length = 30)
    private String transferReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}