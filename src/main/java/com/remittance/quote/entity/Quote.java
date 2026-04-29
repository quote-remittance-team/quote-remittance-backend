package com.remittance.quote.entity;

import com.remittance.common.model.BaseEntity;
import com.remittance.enums.QuoteStatus;
import com.remittance.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "quotes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_quote_reference", columnNames = "quote_reference")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quote extends BaseEntity {

    @Column(name = "quote_reference", nullable = false, updatable = false)
    private String quoteReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "send_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal sendAmount;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6, updatable = false)
    private BigDecimal exchangeRate;

    @Column(name = "fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(name = "receive_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal receiveAmount;

    @Column(name = "total_payable", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPayable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}