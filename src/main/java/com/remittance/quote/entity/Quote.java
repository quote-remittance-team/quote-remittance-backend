package com.remittance.quote.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @Column(precision = 18, scale = 2)
    private BigDecimal sendAmount;

    @Enumerated(EnumType.STRING)
    @Setter
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Setter
    private Currency toCurrency;

    @Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(precision = 18, scale = 2)
    private BigDecimal receiveAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalPayable;

    @Enumerated(EnumType.STRING)
    @Setter
    private Status status ;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Setter
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
