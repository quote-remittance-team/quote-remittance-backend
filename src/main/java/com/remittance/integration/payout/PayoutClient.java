package com.remittance.integration.payout;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.remittance.integration.payout.dto.TransferRecipientRequest;
import com.remittance.integration.payout.dto.TransferRecipientResponse;
import com.remittance.integration.payout.dto.TransferRequest;
import com.remittance.integration.payout.dto.TransferResponse;

@Component
public class PayoutClient {
    private final RestClient restClient;
    public PayoutClient(RestClient paystackRestClient)  {
        this.restClient = paystackRestClient;
    }
    public TransferRecipientResponse createTransferRecipient(TransferRecipientRequest request) {
        return restClient.post().uri("/transferrecipient").body(request).retrieve().body(TransferRecipientResponse.class);
    }
    public TransferResponse initiateTransfer(TransferRequest request) {
        return restClient.post().uri("/transfer").body(request).retrieve().body(TransferResponse.class);
    }
}
