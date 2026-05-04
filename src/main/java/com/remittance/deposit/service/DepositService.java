package com.remittance.deposit.service;

import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.dto.DepositResponseDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.integration.payment.PaymentClient;
import com.remittance.integration.payment.dto.PaystackInitializeRequestDto;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j

public class DepositService {
    private final DepositRepository depositRepository;
    private final QuoteRepository quoteRepository;
    private final PaymentClient paymentClient;

    public DepositResponseDto initiateDeposit(DepositRequestDto request, String customerEmail) {
        Quote quote = quoteRepository.findById(request.getQuoteId()).orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        String currency = quote.getFromCurrency();
        BigDecimal multiplier = getCurrencyMultiplier(currency);
        String amountInSubunits = quote.getTotalPayable()
                .multiply(multiplier)
                .toBigInteger()
                .toString();

        //Generate a unique tracking reference
        String paymentReference = UUID.randomUUID().toString();

        PaystackInitializeRequestDto paystackRequest = PaystackInitializeRequestDto.builder()
                .email(customerEmail)
                .amount(amountInSubunits)
                .reference(paymentReference)
                .build();

        var paystackResponse = paymentClient.initializeTransaction(paystackRequest);
        if (paystackResponse == null || !paystackResponse.isStatus() || paystackResponse.getData() == null) {
            log.error("Paystack initialization failed or returned null data for reference: {}", paymentReference);
            throw new RuntimeException("Payment gateway initialization failed");
        }
        Deposit deposit =  Deposit.builder()
                .quote(quote)
                .amount(quote.getTotalPayable())
                .currency(currency)
                .status(DepositStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .paymentReference(paystackResponse.getData().getReference())
                .build();

        deposit = depositRepository.save(deposit);
        return DepositResponseDto.builder()
                .id(deposit.getId())
                .amount(deposit.getAmount())
                .currency(deposit.getCurrency())
                .status(deposit.getStatus())
                .checkoutUrl(paystackResponse.getData().getAuthorisationUrl())
                .build();

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

    private BigDecimal getCurrencyMultiplier(String currency) {
        if (currency == null) return BigDecimal.valueOf(100);
        return switch (currency.toUpperCase()) {
            case "JPY", "KRW", "VND" -> BigDecimal.ONE;
            case "KWD", "BHD", "OMR", "JOD" -> BigDecimal.valueOf(1000);
            default -> BigDecimal.valueOf(100);
        };
    }
}
