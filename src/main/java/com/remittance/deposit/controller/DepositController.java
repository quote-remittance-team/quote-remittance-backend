package com.remittance.deposit.controller;

import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.dto.DepositWebhookDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/deposits")
@RequiredArgsConstructor
@Slf4j

public class DepositController {
    private final DepositService depositService;
    @Value("${remittance.webhook.secret}")
    private String expectedWebhookSecret;

    // POST/deposits
    @PostMapping
    public ResponseEntity<Deposit> createDeposit(@Valid  @RequestBody DepositRequestDto request) {
        log.info("Received request to initiate deposit for quote ID {}", request.getQuoteId());
        Deposit createdDeposit = depositService.initiateDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeposit);
    }
    // POST /deposits/webhook
    @PostMapping("/webhook")
    public ResponseEntity<Deposit> handleWebhook(@Valid @RequestBody DepositWebhookDto webhookPayload, @RequestHeader(value = "X-Provider-Signature", required = false) String providedSignature) {
        log.info("Received webhook for payment reference: {} with status: {}", webhookPayload.getPaymentReference(), webhookPayload.getStatus());
        if (providedSignature == null || !java.security.MessageDigest.isEqual(providedSignature.getBytes(java.nio.charset.StandardCharsets.UTF_8),expectedWebhookSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            log.warn("SECURITY ALERT: Unauthorized webhook spoofing attempt blocked!");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid webhook spoofing attempt blocked!");
        }
        log.info("Webhook signature verified successfully!");
        Deposit updateDeposit = depositService.handlePaymentCallback(webhookPayload.getPaymentReference(), webhookPayload.getStatus());
        return ResponseEntity.ok(updateDeposit);
    }

}
