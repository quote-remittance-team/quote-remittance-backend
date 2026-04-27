package com.remittance.payout.entity;

import com.remittance.common.model.BaseEntity;
import com.remittance.enums.PayoutStatus;
import com.remittance.remittance.entity.Remittance;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "payouts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payout_provider_reference", columnNames = "provider_reference"),
                @UniqueConstraint(name = "uk_payout_remittance", columnNames = "remittance_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payout extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remittance_id", nullable = false)
    private Remittance remittance;

    @Setter(AccessLevel.NONE)
    @Column(name = "provider_reference", nullable = false, updatable = false)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;
}