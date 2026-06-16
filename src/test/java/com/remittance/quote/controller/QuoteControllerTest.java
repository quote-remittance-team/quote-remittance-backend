package com.remittance.quote.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.auth.security.JwtAuthenticationFilter;
import com.remittance.auth.security.JwtService;
import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.service.QuoteService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(QuoteController.class)
@AutoConfigureMockMvc // FILTERS ARE ON SO ANONYMOUS CHECKS WORK
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuoteService quoteService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Your custom filter mock

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUpSecurityFilterBypass() throws Exception {
        // Tells your mocked JwtFilter to stop blocking requests and just pass them along!
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
                                .with(csrf()) // Keeps CSRF configuration satisfied
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quoteId").value(quoteId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestWhenCurrenciesAreSame() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateQuoteRequest request = CreateQuoteRequest.builder()
                .userId(userId)
                .sendAmount(BigDecimal.valueOf(100))
                .fromCurrency("USD")
                .toCurrency("USD")
                .build();

        when(quoteService.generateQuote(any(CreateQuoteRequest.class)))
                .thenThrow(new IllegalArgumentException("Source and destination currencies cannot be the same"));

        mockMvc.perform(
                        post("/quotes")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com") // Forces a real UsernamePasswordAuthenticationToken (Not anonymous!)
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
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").value(quoteId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnNotFoundWhenQuoteDoesNotExist() throws Exception {
        UUID quoteId = UUID.randomUUID();

        when(quoteService.getQuoteById(quoteId, "test@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));

        mockMvc.perform(
                        get("/quotes/{id}", quoteId)
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
    }
}