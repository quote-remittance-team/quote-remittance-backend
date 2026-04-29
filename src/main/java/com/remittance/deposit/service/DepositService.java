package com.remittance.deposit.service;

import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.deposit.entity.DepositStatus;
import com.remittance.enums.Currency;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class DepositService {
    private final DepositRepository depositRepository;

    private final QuoteRepository quoteRepository;

    @Transactional
    public Deposit initiateDeposit(DepositRequestDto request) {
        Quote quote = quoteRepository.findById(request.getQuoteId()).orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        String currency = quote.getFromCurrency();
        Deposit deposit =  Deposit.builder()
                .quote(quote)
                .amount(quote.getTotalPayable())
                .currency(currency)
                .status(DepositStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        return depositRepository.save(deposit);
    }
    @Transactional
    public Deposit handlePaymentCallback(String paymentReference, DepositStatus newStatus) {
        Deposit deposit = depositRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found for reference:" + paymentReference));

        deposit.setStatus(newStatus);
        return depositRepository.save(deposit);
    }

    @Transactional
    public Deposit updateDepositStatus(UUID depositId, DepositStatus newStatus) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found for id:" + depositId));

        deposit.setStatus(newStatus);
        return depositRepository.save(deposit);
    }
}
