package com.remittance.deposit.entity;

import com.remittance.common.model.BaseEntity;
import com.remittance.enums.DepositStatus;
import com.remittance.quote.entity.Quote;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "deposits",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_deposit_quote", columnNames = "quote_id"),
                @UniqueConstraint(name = "uk_deposit_payment_reference", columnNames = "payment_reference"),
                @UniqueConstraint(name = "uk_deposit_idempotency", columnNames = "idempotency_key")
        },
        indexes = {
                @Index(name = "idx_deposit_quote", columnList = "quote_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Deposit extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status;

    @Column(name = "payment_reference", updatable = false)
    private String paymentReference;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;
}
