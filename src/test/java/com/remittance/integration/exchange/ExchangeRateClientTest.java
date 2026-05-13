package com.remittance.integration.exchange;

import com.remittance.integration.exchange.dto.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateClient exchangeRateClient;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                exchangeRateClient,
                "apiUrl",
                "https://v6.exchangerate-api.com/v6"
        );

        ReflectionTestUtils.setField(
                exchangeRateClient,
                "apiKey",
                "test-api-key"
        );
    }

    @Test
    void shouldReturnExchangeRateSuccessfully() {

        ExchangeRateResponse response =
                new ExchangeRateResponse();

        response.setConversionRates(
                Map.of(
                        "NGN",
                        BigDecimal.valueOf(1600)
                )
        );

        when(restTemplate.getForObject(
                anyString(),
                eq(ExchangeRateResponse.class)
        )).thenReturn(response);

        BigDecimal rate =
                exchangeRateClient.getExchangeRate(
                        "USD",
                        "NGN"
                );

        assertNotNull(rate);

        assertEquals(
                BigDecimal.valueOf(1600),
                rate
        );

        verify(restTemplate, times(1))
                .getForObject(
                        anyString(),
                        eq(ExchangeRateResponse.class)
                );
    }

    @Test
    void shouldThrowExceptionWhenResponseIsNull() {

        when(restTemplate.getForObject(
                anyString(),
                eq(ExchangeRateResponse.class)
        )).thenReturn(null);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> exchangeRateClient.getExchangeRate(
                                "USD",
                                "NGN"
                        )
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        assertEquals(
                "Exchange rate provider unavailable",
                exception.getReason()
        );
    }

    @Test
    void shouldThrowExceptionWhenCurrencyRateIsMissing() {

        ExchangeRateResponse response =
                new ExchangeRateResponse();

        response.setConversionRates(
                Map.of(
                        "EUR",
                        BigDecimal.valueOf(0.92)
                )
        );

        when(restTemplate.getForObject(
                anyString(),
                eq(ExchangeRateResponse.class)
        )).thenReturn(response);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> exchangeRateClient.getExchangeRate(
                                "USD",
                                "NGN"
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Unsupported currency",
                exception.getReason()
        );
    }

    @Test
    void shouldThrowExceptionWhenProviderFails() {

        when(restTemplate.getForObject(
                anyString(),
                eq(ExchangeRateResponse.class)
        )).thenThrow(
                new RestClientException("Provider error")
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> exchangeRateClient.getExchangeRate(
                                "USD",
                                "NGN"
                        )
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        assertEquals(
                "Exchange rate provider unavailable",
                exception.getReason()
        );
    }
}
