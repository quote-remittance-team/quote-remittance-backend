package com.remittance.integration.payout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import com.remittance.integration.payout.dto.TransferRecipientRequest;
import com.remittance.integration.payout.dto.TransferRecipientResponse;
import com.remittance.integration.payout.dto.TransferRequest;
import com.remittance.integration.payout.dto.TransferResponse;

@Component
public class PayoutClient {
    private final RestClient restClient;
    public PayoutClient(RestClient.Builder restClientBuilder, @Value("${paystack.api.secret}") String paystackApiSecret) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.paystack.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paystackApiSecret)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    public TransferRecipientResponse createTransferRecipient(TransferRecipientRequest request) {
        return restClient.post().uri("/transferrecipient").body(request).retrieve().body(TransferRecipientResponse.class);
    }
    public TransferResponse initiateTransfer(TransferRequest request) {
        return restClient.post().uri("/transfer").body(request).retrieve().body(TransferResponse.class);
    }
}
