package com.remittance.quote.service;

import com.remittance.integration.exchange.ExchangeRateClient;
import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import com.remittance.quote.service.impl.QuoteServiceImpl;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    // ✅ Define a reusable mock email for your security context lookup
    private final String mockEmail = "testuser@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(quoteService, "feePercentage", BigDecimal.valueOf(0.01));
        ReflectionTestUtils.setField(quoteService, "quoteExpiryMinutes", 30L);
    }

    @Test
    void shouldGenerateQuoteSuccessfully() {

        CreateQuoteRequest request = new CreateQuoteRequest(
                BigDecimal.valueOf(100),
                "USD",
                "NGN"
        );

        User user = User.builder().build();

        // ✅ FIXED: Mocking findByEmailIgnoreCase instead of the old findById lookup
        when(userRepository.findByEmailIgnoreCase(mockEmail))
                .thenReturn(Optional.of(user));

        when(quoteRepository.save(any(Quote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(exchangeRateClient.getExchangeRate("USD", "NGN"))
                .thenReturn(BigDecimal.valueOf(1600));

        // ✅ FIXED: Passing BOTH the request payload and the secure user email string
        QuoteResponse response = quoteService.generateQuote(request, mockEmail);

        assertNotNull(response);
        assertEquals("USD", response.getFromCurrency());
        assertEquals("NGN", response.getToCurrency());
        assertNotNull(response.getQuoteReference());

        verify(quoteRepository).save(any(Quote.class));
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesAreTheSame() {
        // ✅ FIXED: Testing your active service check: "Source and destination currencies cannot be the same"
        CreateQuoteRequest request = new CreateQuoteRequest(
                BigDecimal.valueOf(100),
                "USD",
                "USD"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quoteService.generateQuote(request, mockEmail)
        );

        assertEquals(
                "Source and destination currencies cannot be the same",
                exception.getMessage()
        );
    }
}