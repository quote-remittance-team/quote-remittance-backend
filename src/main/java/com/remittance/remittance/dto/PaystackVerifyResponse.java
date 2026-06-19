package com.remittance.remittance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackVerifyResponse {
    private boolean status;
    private String message;
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private String status;
        private String reference;
        private Long amount; // Amount returned in minor units (kobo)
    }

}
