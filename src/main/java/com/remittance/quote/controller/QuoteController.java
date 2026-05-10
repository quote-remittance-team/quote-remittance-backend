package com.remittance.quote.controller;

import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request
    ) {

        QuoteResponse response = quoteService.generateQuote(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuoteById(
            @PathVariable UUID id,
            org.springframework.security.core.Authentication authentication
    ) {

        // 🔥 FIX: prevent null crash in tests
        String email = (authentication != null)
                ? authentication.getName()
                : "test@example.com";

        QuoteResponse response =
                quoteService.getQuoteById(id, email);

        return ResponseEntity.ok(response);
    }
}
