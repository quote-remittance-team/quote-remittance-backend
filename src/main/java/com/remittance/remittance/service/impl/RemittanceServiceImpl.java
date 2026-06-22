package com.remittance.remittance.service.impl;

import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.enums.RemittanceStatus;
import com.remittance.quote.entity.Quote;
import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.entity.Remittance;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import com.remittance.remittance.repository.RemittanceRepository;
import com.remittance.remittance.service.RemittanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemittanceServiceImpl implements RemittanceService {

    private final DepositRepository depositRepository;
    private final RemittanceRepository remittanceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${paystack.api.secret}")
    private String paystackSecretKey;

    @Override
    @Transactional
    public RemittanceResponse createRemittance(
            CreateRemittanceRequest request,
            String userEmail
    ) {

        Deposit deposit = depositRepository.findById(request.getDepositId())
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        /*
         * SECURITY CHECK
         * Prevent IDOR attacks
         *  */
        if (!deposit.getQuote()
                .getUser()
                .getEmail()
                .equals(userEmail)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Unauthorized access to deposit"
            );
        }

        if (deposit.getStatus() != DepositStatus.CONFIRMED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Deposit must be confirmed before remittance"
            );
        }

        remittanceRepository.findByDepositId(deposit.getId())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Remittance already exists for deposit"
                    );
                });

        Optional<Remittance> existingRemittance =
                remittanceRepository.findByIdempotencyKey(
                        request.getIdempotencyKey()
                );
        if (existingRemittance.isPresent()) {

            return mapToResponse(existingRemittance.get());
        }

        Quote quote = deposit.getQuote();

        Remittance remittance = Remittance.builder()
                .reference(generateReference())
                .deposit(deposit)
                .quote(quote)
                .sender(quote.getUser())
                .receiverName(request.getReceiverName())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverBankCode(request.getReceiverBankCode())
                .sendAmount(quote.getSendAmount())
                .receiveAmount(quote.getReceiveAmount())
                .exchangeRate(quote.getExchangeRate())
                .status(RemittanceStatus.PROCESSING)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Remittance savedRemittance = remittanceRepository.save(remittance);

        eventPublisher.publishEvent(
                new RemittanceCreatedEvent(savedRemittance)
        );

        return RemittanceResponse.builder()
                .remittanceId(savedRemittance.getId())
                .reference(savedRemittance.getReference())
                .sendAmount(savedRemittance.getSendAmount())
                .receiveAmount(savedRemittance.getReceiveAmount())
                .status(savedRemittance.getStatus())
                .createdAt(savedRemittance.getCreatedAt())
                .build();
    }

    @Override
    public RemittanceResponse getByReference(String reference, String email) {

        Remittance remittance = remittanceRepository
                .findByReference(reference)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Remittance not found"
                        )
                );

        if (!remittance.getSender()
                .getEmail()
                .equals(email)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Unauthorized access to remittance"
            );
        }

        return mapToResponse(remittance);
    }

    private  String generateReference() {

        return "RMT-" + UUID.randomUUID()
                .toString()
                .toUpperCase();
    }

    private RemittanceResponse mapToResponse(
            Remittance remittance
    ) {

        return RemittanceResponse.builder()
                .remittanceId(remittance.getId())
                .reference(remittance.getReference())
                .status(remittance.getStatus())
                .sendAmount(remittance.getSendAmount())
                .receiveAmount(remittance.getReceiveAmount())
                .createdAt(remittance.getCreatedAt())
                .build();
    }

    @Override
    public RemittanceResponse verifyAndCompletePayment(String reference, String userEmail) {
        log.info("Executing relational processing logic verification for reference: {}", reference);

        // Check if it already exists before doing anything else
        Optional<Remittance> existingRemittance = remittanceRepository.findByReference(reference);
        if (existingRemittance.isPresent()) {

            if (!existingRemittance.get().getSender().getEmail().equals(userEmail)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to transaction metadata.");
            }
            log.info("Duplicate request intercepted safely via reference tracking keys. Returning existing entity.");
            return mapToResponse(existingRemittance.get());
        }

        // 2. Query Paystack Secure Gateway to Audit the Payment State
        String paystackVerificationUrl = "https://api.paystack.co/transaction/verify/" + reference;
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        // FIXED: Added missing space after "Bearer "
        headers.set("Authorization", "Bearer " + this.paystackSecretKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(headers);

        try {
            log.info("Sending Authorization Header: Bearer {}", this.paystackSecretKey != null ? "PRESENT (Length: " + this.paystackSecretKey.length() + ")" : "NULL");
            log.info("Target Paystack Verification URL: {}", paystackVerificationUrl);

// Dispatching outbound validation call...
            log.info("Dispatching outbound validation call to Paystack API...");
            org.springframework.http.ResponseEntity<com.remittance.remittance.dto.PaystackVerifyResponse> apiResponse =
                    restTemplate.exchange(
                            paystackVerificationUrl,
                            org.springframework.http.HttpMethod.GET,
                            requestEntity,
                            com.remittance.remittance.dto.PaystackVerifyResponse.class
                    );

            com.remittance.remittance.dto.PaystackVerifyResponse payload = apiResponse.getBody();

            if (payload == null || !payload.isStatus() || !"success".equals(payload.getData().getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment confirmation rejected by gateway authority.");
            }

            // 3. Save the data inside a dedicated transaction block
            return saveVerifiedTransaction(reference, userEmail);

        } catch (org.springframework.web.client.RestClientException exception) {
            log.error("Network infrastructure dropout during gateway verification exchange: ", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Outbound financial validation communication error.");
        }
    }

    @Transactional
    public RemittanceResponse saveVerifiedTransaction(String reference, String userEmail) {
        // Locate corresponding PENDING row
        Deposit deposit = depositRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No matching deposit asset index matches reference: " + reference));

        // Anti-IDOR Check
        if (!deposit.getQuote().getUser().getEmail().equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access authorization check failed for this entity.");
        }

        // Update Funding Source state
        deposit.setStatus(DepositStatus.CONFIRMED);
        depositRepository.save(deposit);

        // Compile Remittance record
        Quote quote = deposit.getQuote();
        Remittance remittance = Remittance.builder()
                .reference(reference)
                .deposit(deposit)
                .quote(quote)
                .sender(quote.getUser())
                .receiverName(deposit.getReceiverName())
                .receiverAccountNumber(deposit.getReceiverAccountNumber())
                .receiverBankCode(deposit.getReceiverBankCode())
                .sendAmount(quote.getSendAmount())
                .receiveAmount(quote.getReceiveAmount())
                .exchangeRate(quote.getExchangeRate())
                .status(RemittanceStatus.PROCESSING)
                .idempotencyKey("IDEM-" + reference)
                .build();

        Remittance savedRemittance = remittanceRepository.save(remittance);

        // Broadcast downstream events
        eventPublisher.publishEvent(new RemittanceCreatedEvent(savedRemittance));

        return mapToResponse(savedRemittance);
    }
}
