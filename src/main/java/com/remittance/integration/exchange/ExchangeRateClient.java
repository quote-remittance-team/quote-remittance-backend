package com.remittance.integration.exchange;

import com.remittance.integration.exchange.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateClient {

    private final RestTemplate restTemplate;

    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Cacheable(
            value = "exchangeRates",
            key = "#fromCurrency + '-' + #toCurrency"
    )
    public BigDecimal getExchangeRate(
            String fromCurrency,
            String toCurrency
    ) {

        String url = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .pathSegment(
                        apiKey,
                        "latest",
                        fromCurrency
                )
                .toUriString();

        try {

            ExchangeRateResponse response =
                    restTemplate.getForObject(
                            url,
                            ExchangeRateResponse.class
                    );

            if (response == null
                    || response.getConversionRates() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Exchange rate provider unavailable"
                );
            }

            BigDecimal rate =
                    response.getConversionRates()
                            .get(toCurrency);

            if (rate == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported currency"
                );
            }

            log.info(
                    "Fetched exchange rate {} -> {} = {}",
                    fromCurrency,
                    toCurrency,
                    rate
            );

            return rate;

        } catch (RestClientException ex) {

            log.error(
                    "Exchange rate provider failed",
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Exchange rate provider unavailable"
            );
        }
    }
}
