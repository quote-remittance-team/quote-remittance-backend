package com.remittance.quote.service;

import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import com.remittance.quote.service.impl.QuoteServiceImpl;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    @Test
    void shouldGenerateQuoteSuccessfully() {

        UUID userId = UUID.randomUUID();

        CreateQuoteRequest request = new CreateQuoteRequest(
                userId,
                BigDecimal.valueOf(100),
                "USD",
                "NGN"
        );

        User user = User.builder().build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(quoteRepository.save(any(Quote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuoteResponse response = quoteService.generateQuote(request);

        assertNotNull(response);
        assertEquals("USD", response.getFromCurrency());
        assertEquals("NGN", response.getToCurrency());
        assertNotNull(response.getQuoteReference());

        verify(quoteRepository).save(any(Quote.class));
    }

    @Test
    void shouldThrowExceptionForUnsupportedCurrency() {

        UUID userId = UUID.randomUUID();

        CreateQuoteRequest request = new CreateQuoteRequest(
                userId,
                BigDecimal.valueOf(100),
                "ABC",
                "NGN"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quoteService.generateQuote(request)
        );

        assertEquals(
                "Unsupported currency",
                exception.getMessage()
        );
    }
}
