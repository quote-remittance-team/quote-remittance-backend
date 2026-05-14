package com.remittance.integration.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExchangeRateResponse {

    private String result;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;
}
