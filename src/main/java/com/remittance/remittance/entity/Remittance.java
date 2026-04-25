package com.remittance.remittance.entity;

import com.remittance.common.model.BaseEntity;
import com.remittance.deposit.entity.Deposit;
import com.remittance.enums.RemittanceStatus;
import com.remittance.quote.entity.Quote;
import com.remittance.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "remittances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_remittance_reference", columnNames = "reference"),
                @UniqueConstraint(name = "uk_remittance_deposit", columnNames = "deposit_id"),
                @UniqueConstraint(name = "uk_remittance_idempotency", columnNames = "idempotency_key")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Remittance extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", nullable = false)
    private Deposit deposit;

    @Column(nullable = false)
    private String reference;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_account_number", nullable = false)
    private String receiverAccountNumber;

    @Column(name = "receiver_bank_code", nullable = false)
    private String receiverBankCode;

    @Column(name = "send_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal sendAmount;

    @Column(name = "receive_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal receiveAmount;

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RemittanceStatus status;

    @Column(name = "idempotency_key")
    private String idempotencyKey;
}
