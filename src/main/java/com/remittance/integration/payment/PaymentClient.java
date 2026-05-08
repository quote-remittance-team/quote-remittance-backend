package com.remittance.integration.payment;

import com.remittance.integration.payment.dto.PaystackInitializeRequestDto;
import com.remittance.integration.payment.dto.PaystackInitializeResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class PaymentClient {
    private final RestClient restClient;

    public PaymentClient(@Qualifier("paystackRestClient")  RestClient restClient) {
        this.restClient = restClient;
        log.info("Payment Envoy successfully equipped with secure Paystack RestClient");
    }

    public PaystackInitializeResponseDto initializeTransaction(PaystackInitializeRequestDto requestDto) {
        log.info("Envoy initiating Paystack transaction for email: {}", requestDto.getEmail());
        try {
            return restClient.post().uri("/transaction/initialize").contentType(MediaType.APPLICATION_JSON).body(requestDto).retrieve().body(PaystackInitializeResponseDto.class);
        } catch (RestClientException e) {
            log.error("Envoy failed to communicate with Paystack Bank: {}", e.getMessage());
            throw new RuntimeException("Payment gateway integration failed", e);
        }
    }
}
