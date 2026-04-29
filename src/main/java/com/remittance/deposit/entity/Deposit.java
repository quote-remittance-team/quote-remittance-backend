package com.remittance.deposit.entity;

import com.remittance.common.model.BaseEntity;
import com.remittance.enums.DepositStatus;
import com.remittance.quote.entity.Quote;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// Lombok annotations
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "deposits")
public class Deposit extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", referencedColumnName = "id", nullable = false, unique = true)
    private Quote quote;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "idempotency_key",unique = true, nullable = false)
    private String idempotencyKey;
}
