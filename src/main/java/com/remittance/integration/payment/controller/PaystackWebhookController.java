package com.remittance.integration.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.deposit.service.DepositService;
import com.remittance.enums.DepositStatus;
import com.remittance.integration.payment.dto.PaystackWebhookEventDto;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks/paystack")
@RequiredArgsConstructor
@Slf4j
public class PaystackWebhookController {
    private final DepositService depositService;
    private final ObjectMapper objectMapper;

    @Value("${paystack.api.secret}")
    private String paystackApiSecret;

    @PostMapping
    public ResponseEntity<Void> handlePaystackWebhook(@RequestHeader(value = "x-paystack-signature", required = false) String signature, @RequestBody String rawPayload) {
        log.info("Received Webhook from Paystack...");
        if (signature == null || !isValidSignature(rawPayload, signature)) {
            log.warn("Invalid or missing Paystack webhook signature! Intruder alert!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PaystackWebhookEventDto eventDto = objectMapper.readValue(rawPayload, PaystackWebhookEventDto.class);
            if ("charge.success".equals(eventDto.getEvent())) {
                String paymentRefence = eventDto.getData().getReference();
                log.info("Payment successful for refence {}", paymentRefence);
                depositService.handlePaymentCallback(paymentRefence, DepositStatus.CONFIRMED);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process paystack webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    private boolean isValidSignature(String rawPayload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(paystackApiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String generatedSignature = hexString.toString();
            return MessageDigest.isEqual(generatedSignature.getBytes(StandardCharsets.UTF_8),signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error generating HMAC SHA512 signature", e);
            return false;
        }
    }
}
