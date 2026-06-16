package com.remittance.remittance.controller;

import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.service.RemittanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/remittances")
@RequiredArgsConstructor
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
}
