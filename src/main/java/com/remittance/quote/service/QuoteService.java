package com.remittance.quote.service;

import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;

import java.util.UUID;

public interface QuoteService {

    QuoteResponse generateQuote(CreateQuoteRequest request);
    QuoteResponse getQuoteById(UUID id);
}
