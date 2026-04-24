package com.remittance.quote.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String quoteReference;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 18, scale = 2)
    private BigDecimal sendAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency toCurrency;

    @Column( nullable = false, precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(precision = 18, scale = 2)
    private BigDecimal receiveAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalPayable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status ;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    public enum Currency {
        USD,
        EUR,
        GBP,
        NGN
    }

    public enum Status{
        ACTIVE,
        USED,
        EXPIRED
    }
}
