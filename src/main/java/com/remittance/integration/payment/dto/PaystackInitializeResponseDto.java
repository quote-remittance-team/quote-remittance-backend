package com.remittance.integration.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackInitializeResponseDto {
    private boolean status;
    private String message;
    private PaystackData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackData {
        @JsonProperty("authorisation_url")
        private String authorisationUrl;

        @JsonProperty("access_code")
        private String accessCode;

        private String reference;
    }
}
