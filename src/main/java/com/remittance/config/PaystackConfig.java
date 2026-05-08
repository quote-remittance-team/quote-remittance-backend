package com.remittance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class PaystackConfig {
    @Bean(name = "paystackRestClient")
    public RestClient paystackRestClient(@Value("${paystack.api.url}") String paystackUrl, @Value("${paystack.api.secret}") String paystackApiSecret) {
        return RestClient.builder()
                .baseUrl(paystackUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paystackApiSecret)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }
}

