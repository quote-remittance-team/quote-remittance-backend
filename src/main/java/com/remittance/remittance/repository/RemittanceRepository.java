package com.remittance.remittance.repository;

import com.remittance.remittance.entity.Remittance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RemittanceRepository extends JpaRepository<Remittance, UUID> {

    Optional<Remittance> findByReference(String reference);

    Optional<Remittance> findByDepositId(UUID depositId);

    Page<Remittance> findBySender_Id(UUID senderId, Pageable pageable);

}
