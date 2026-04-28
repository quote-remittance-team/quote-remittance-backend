package com.remittance.deposit.service;

import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.quote.entity.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class DepositService {
    private final DepositRepository depositRepository;

    @Transactional
    public Deposit initiateDeposit(Quote quote,  String idempotencyKey) {
        Deposit deposit =  Deposit.builder()
                .quote(quote)
                .amount(quote.getTotalPayable())
                .currency(quote.getFromCurrency())
                .status(DepositStatus.PENDING)
                .idempotencyKey(idempotencyKey)
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
