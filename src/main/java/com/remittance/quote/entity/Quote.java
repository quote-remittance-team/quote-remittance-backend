package com.remittance.quote.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(precision = 18, scale = 2)
    private BigDecimal sendAmount;

    @Enumerated(EnumType.STRING)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    private Currency toCurrency;

    @Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(precision = 18, scale = 2)
    private BigDecimal recieveAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalPayable;

    @Enumerated(EnumType.STRING)
    private Status status ;

    private LocalDateTime createdAt;

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
