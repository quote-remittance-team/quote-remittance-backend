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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final DepositRepository depositRepository;
    private final QuoteRepository quoteRepository;
    private final PaymentClient paymentClient;

    /**
     * Orchestrates the deposit initiation workflow.
     * Note: This method is intentionally NOT @Transactional to protect the Hikari connection pool
     * during the outbound network call to Paystack.
     */
    public DepositResponseDto initiateDeposit(DepositRequestDto request, String customerEmail) {
        // 1. Fetch the quote information out of a long-running transaction context
        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        String currency = quote.getFromCurrency();
        BigDecimal amountInNaira = currency.equalsIgnoreCase("NGN")
                ? quote.getTotalPayable()
                : quote.getTotalPayable().multiply(quote.getExchangeRate());

        String amountInSubunits = amountInNaira
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .toBigInteger().toString();

        // 2. Generate a tracking reference locally before talking to external providers
        String paymentReference = UUID.randomUUID().toString();

        // 3. PERSIST FIRST: Save the record to the database in a PENDING state.
        // This closes the code review gap. If the application crashes next, the transaction is already logged safely.
        Deposit deposit = saveInitialPendingDeposit(request, quote, currency, paymentReference);

        try {
            PaystackInitializeRequestDto paystackRequest = PaystackInitializeRequestDto.builder()
                    .email(customerEmail)
                    .amount(amountInSubunits)
                    .reference(paymentReference)
                    .build();

            // 4. Run the slow internet handshake. Hikari database slots are completely safe here!
            log.info("Envoy communicating with Paystack API safely outside of database boundary for reference: {}", paymentReference);
            var paystackResponse = paymentClient.initializeTransaction(paystackRequest);

            if (paystackResponse == null || !paystackResponse.isStatus() || paystackResponse.getData() == null) {
                log.error("Paystack initialization returned an invalid response for reference: {}", paymentReference);
                throw new RuntimeException("Payment gateway initialization returned empty payload");
            }

            // 5. Build and return the response payload carrying the authorization checkout link
            return DepositResponseDto.builder()
                    .id(deposit.getId())
                    .amount(deposit.getAmount())
                    .currency(deposit.getCurrency())
                    .status(deposit.getStatus())
                    .checkoutUrl(paystackResponse.getData().getAuthorizationUrl())
                    .paymentReference(deposit.getPaymentReference())
                    .receiverName(deposit.getReceiverName())
                    .receiverAccountNumber(deposit.getReceiverAccountNumber())
                    .receiverBankCode(deposit.getReceiverBankCode())
                    .build();

        } catch (Exception e) {
            log.error("Paystack network transaction failed for reference: {}. Rolling back local state to FAILED.", paymentReference, e);

            // 6. SAFEGUARD: Fallback mechanism to update the pre-persisted record status if the network call dropped
            updateDepositStatusToFailed(deposit.getId());

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment gateway integration communication failed");
        }
    }

    /**
     * Isolated transaction block used to register the initial record safely into PostgreSQL.
     */
    @Transactional
    public Deposit saveInitialPendingDeposit(DepositRequestDto request, Quote quote, String currency, String reference) {
        Deposit deposit = Deposit.builder()
                .quote(quote)
                .amount(quote.getTotalPayable())
                .currency(currency)
                .status(DepositStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .paymentReference(reference)
                .receiverName(request.getReceiverName())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverBankCode(request.getReceiverBankCode())
                .build();

        return depositRepository.save(deposit);
    }

    /**
     * Isolated transaction block used to cleanly move failed instantiations to a dead status state.
     */
    @Transactional
    public void updateDepositStatusToFailed(UUID depositId) {
        depositRepository.findById(depositId).ifPresent(deposit -> {
            deposit.setStatus(DepositStatus.FAILED);
            depositRepository.save(deposit);
            log.info("Deposit record ID {} successfully set to FAILED state.", depositId);
        });
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

    public Deposit getByPaymentReference(String paymentReference) {
        return depositRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() ->
                        new IllegalArgumentException("Deposit not found: " + paymentReference));
    }
}