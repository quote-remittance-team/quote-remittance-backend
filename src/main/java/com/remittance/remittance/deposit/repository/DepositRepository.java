package com.remittance.remittance.deposit.repository;

import com.remittance.deposit.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface DepositRepository  extends JpaRepository<Deposit, UUID> {

    //find deposit connected by Quote's UUID
    Optional<Deposit> findByQuoteId(UUID quoteId);

    //find deposit using payment gateway's reference receipt
    Optional<Deposit> findByPaymentReference(String paymentReference);
}
