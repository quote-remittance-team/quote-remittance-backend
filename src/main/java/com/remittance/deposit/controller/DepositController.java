package com.remittance.deposit.controller;

import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.dto.DepositWebhookDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/deposits")
@RequiredArgsConstructor
@Slf4j

public class DepositController {
    private final DepositService depositService;

    // POST/deposits
    @PostMapping
    public ResponseEntity<Deposit>createDeposit(@RequestBody DepositRequestDto request) {
        log.info("Received request to initiate deposit for quote ID {}", request.getQuoteId());
        Deposit createdDeposit = depositService.initiateDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeposit);
    }
    // POST /deposits/webhook
    @PostMapping("/webhook")
    public ResponseEntity<Deposit>handleWebhook(@RequestBody DepositWebhookDto webhookPayload) {
        log.info("Received webhook for payment reference: {} with status: {}", webhookPayload.getPaymentReference(), webhookPayload.getStatus());
        Deposit updateDeposit = depositService.handlePaymentCallback(webhookPayload.getPaymentReference(), webhookPayload.getStatus());
        return ResponseEntity.ok(updateDeposit);
    }

}
