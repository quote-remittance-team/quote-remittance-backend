package com.remittance.quote.controller;

import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse createQuote(
            @Valid @RequestBody CreateQuoteRequest request
    ) {

        return quoteService.generateQuote(request);
    }

    @GetMapping("/{id}")
    public QuoteResponse getQuoteById(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        return quoteService.getQuoteById(id, authentication.getName());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {
        return Map.of(
                "error",
                ex.getMessage()
        );
    }
}
