package com.remittance.remittance.controller;

import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.service.RemittanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody CreateRemittanceRequest request
    ) {

        RemittanceResponse response =
                remittanceService.createRemittance(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<RemittanceResponse> getByReference(
            @PathVariable String reference
    ) {

        return ResponseEntity.ok(
                remittanceService.getByReference(reference)
        );
    }
}
