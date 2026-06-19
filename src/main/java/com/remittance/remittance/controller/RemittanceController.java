package com.remittance.remittance.controller;

import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.service.RemittanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/remittances")
@RequiredArgsConstructor
@Slf4j
public class RemittanceController {

    private final RemittanceService remittanceService;

    /**
     * POST /remittances
     * Create/process a remittance
     */
    @PostMapping
    public ResponseEntity<RemittanceResponse> createRemittance(
            @Valid @RequestBody CreateRemittanceRequest request,
            Principal principal
            ) {

        String email = principal.getName();

        RemittanceResponse response =
                remittanceService.createRemittance(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<RemittanceResponse> getByReference(
            @PathVariable String reference,
            Principal principal
    ) {
        String email = principal.getName();

        return ResponseEntity.ok(
                remittanceService.getByReference(reference, "test@example.com")
        );
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<RemittanceResponse> verifyAndCompletePayment(
            @PathVariable String reference,
            Principal principal
    ) {
        if (principal == null) {
            log.warn("Access denied: Principal context is missing for reference verification.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to verify this payment.");
        }

        log.info("Initiating verification protocol for gateway reference: {}", reference);
        String userEmail = principal.getName();
        log.info("Authenticated user context found: {}", userEmail);

        RemittanceResponse response = remittanceService.verifyAndCompletePayment(reference, userEmail);
        return ResponseEntity.ok(response);
    }

}
