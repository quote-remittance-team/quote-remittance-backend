package com.remittance.quote.repository;

import com.remittance.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    List<Quote> findByUser_Id(UUID userId);

    Optional<Quote> findByIdAndUserId(UUID id, UUID userId);
}
