package com.remittance.quote.service;

import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;

public interface QuoteService {

    QuoteResponse generateQuote(CreateQuoteRequest request);
}
