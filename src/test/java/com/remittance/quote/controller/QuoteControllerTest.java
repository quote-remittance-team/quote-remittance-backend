package com.remittance.quote.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(controllers = QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuoteService quoteService;

    @Test
    void shouldGenerateQuoteSuccessfully() throws Exception {

        UUID quoteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateQuoteRequest request = CreateQuoteRequest.builder()
                .userId(userId)
                .sendAmount(BigDecimal.valueOf(100))
                .fromCurrency("USD")
                .toCurrency("NGN")
                .build();

        QuoteResponse response = QuoteResponse.builder()
                .quoteId(quoteId)
                .quoteReference("QTE-123456")
                .sendAmount(BigDecimal.valueOf(100))
                .fromCurrency("USD")
                .toCurrency("NGN")
                .exchangeRate(BigDecimal.valueOf(1600))
                .fee(BigDecimal.valueOf(2))
                .receiveAmount(BigDecimal.valueOf(160000))
                .totalPayable(BigDecimal.valueOf(102))
                .expiresAt(Instant.now())
                .build();

        when(quoteService.generateQuote(any(CreateQuoteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/quotes")
                                .with(csrf())
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quoteId").value(quoteId.toString()))
                .andExpect(jsonPath("$.quoteReference").value("QTE-123456"))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("NGN"));
    }

    @Test
    void shouldReturnBadRequestForInvalidQuoteRequest() throws Exception {

        CreateQuoteRequest request = CreateQuoteRequest.builder()
                .userId(null)
                .sendAmount(null)
                .fromCurrency("NGN")
                .toCurrency("ABC")
                .build();

        mockMvc.perform(
                        post("/quotes")
                                .with(csrf())
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCurrenciesAreSame() throws Exception {

        UUID userId = UUID.randomUUID();

        CreateQuoteRequest request = CreateQuoteRequest.builder()
                .userId(userId)
                .sendAmount(BigDecimal.valueOf(100))
                .fromCurrency("USD")
                .toCurrency("USD")
                .build();

        when(quoteService.generateQuote(any(CreateQuoteRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Source and destination currencies cannot be the same"
                        )
                );

        mockMvc.perform(
                        post("/quotes")
                                .with(csrf())
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnQuoteByIdSuccessfully() throws Exception {

        UUID quoteId = UUID.randomUUID();

        QuoteResponse response = QuoteResponse.builder()
                .quoteId(quoteId)
                .quoteReference("QTE-654321")
                .sendAmount(BigDecimal.valueOf(200))
                .fromCurrency("GBP")
                .toCurrency("NGN")
                .exchangeRate(BigDecimal.valueOf(2100))
                .fee(BigDecimal.valueOf(4))
                .receiveAmount(BigDecimal.valueOf(420000))
                .totalPayable(BigDecimal.valueOf(204))
                .expiresAt(Instant.now())
                .build();

        when(quoteService.getQuoteById(quoteId, "test@example.com"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/quotes/{id}", quoteId)
                                .with(user("test@example.com"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").value(quoteId.toString()))
                .andExpect(jsonPath("$.quoteReference").value("QTE-654321"))
                .andExpect(jsonPath("$.fromCurrency").value("GBP"))
                .andExpect(jsonPath("$.toCurrency").value("NGN"));
    }

    @Test
    void shouldReturnNotFoundWhenQuoteDoesNotExist() throws Exception {

        UUID quoteId = UUID.randomUUID();

        when(quoteService.getQuoteById(quoteId, "test@example.com"))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Quote not found"
                        )
                );

        mockMvc.perform(
                get("/quotes/{id}", quoteId)
                        .with(user("test@example.com"))
        ).andExpect(status().isNotFound());
    }
}
