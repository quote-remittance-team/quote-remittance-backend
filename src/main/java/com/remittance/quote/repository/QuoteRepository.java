package com.remittance.quote.repository;

import com.remittance.quote.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    Page<Quote> findByUser_Id(UUID userId, Pageable pageable);

    Optional<Quote> findByIdAndUser_Id(UUID id, UUID userId);

    Optional<Quote> findByQuoteReference(String quoteReference);
}
