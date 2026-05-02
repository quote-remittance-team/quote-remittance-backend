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

    @Transactional
    public DepositResponseDto initiateDeposit(DepositRequestDto request) {
        Quote quote = quoteRepository.findById(request.getQuoteId()).orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        String currency = quote.getFromCurrency();
        //Paystack amount format(Lowest denomination, multiplies by 100 to remove decimals e.g 100.50 becomes "10050")
        String amountInSubunits = quote.getTotalPayable()
                .multiply(BigDecimal.valueOf(100))
                .toBigInteger()
                .toString();

        //Generate a unique tracking reference
        String paymentReference = UUID.randomUUID().toString();

        String customerEmail = "customer@example.com";
        PaystackInitializeRequestDto paystackRequest = PaystackInitializeRequestDto.builder()
                .email(customerEmail)
                .amount(amountInSubunits)
                .reference(paymentReference)
                .build();

        var paystackResponse = paymentClient.initializeTransaction(paystackRequest);
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
}
