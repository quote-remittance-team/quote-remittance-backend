package com.remittance.payout.entity;

import jakarta.persistence.*;
import lombok.Getter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Getter
@NoArgsConstructor
@Entity
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remittance_id", nullable = false, unique = true )
    private Remittance remittance;

    @Column(nullable = false, unique = true)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }

}
