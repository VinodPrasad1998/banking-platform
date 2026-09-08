package com.nextgenbank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "beneficiaries",
        indexes = {
                @Index(name = "idx_beneficiary_customer", columnList = "customer_id"),
                @Index(name = "idx_beneficiary_account", columnList = "beneficiary_account_number")
        }
)
@Getter
@Setter
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long beneficiaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 100)
    private String beneficiaryName;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(name = "beneficiary_account_number",
            nullable = false,
            length = 30)
    private String beneficiaryAccountNumber;

    @Column(nullable = false, length = 11)
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BeneficiaryStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}