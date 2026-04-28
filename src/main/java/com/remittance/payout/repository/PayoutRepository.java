package com.remittance.payout.repository;

import com.remittance.payout.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Optional<Payout> findByRemittance_Id(UUID remittanceId);
}
