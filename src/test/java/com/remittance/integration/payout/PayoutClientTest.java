package com.remittance.integration.payout;

import com.remittance.integration.payout.dto.TransferRecipientRequest;
import com.remittance.integration.payout.dto.TransferRecipientResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PayoutClientTest {
    @Autowired
    private PayoutClient payoutClient;

    @Test
    void shouldCreateTransferRecipientSuccessfully() {
        TransferRecipientRequest request = new TransferRecipientRequest(
                "nuban",
                "Intgration Test User",
                // use real account number if you want to test it, that's what I did
                "0069087573",
                // use real bank code for testing, that's what i did
                "044",
                "NGN"
        );
        TransferRecipientResponse response = payoutClient.createTransferRecipient(request);
        System.out.println("================================================");
        System.out.println("PAYSTACK RESPONSE STATUS: " + response.status());
        System.out.println("PAYSTACK RECIPIENT CODE: " + response.data().recipientCode());
        System.out.println("================================================");
        assertTrue(response.status(), "Paystack should return a true status");
        assertNotNull(response.data().recipientCode(), "The speed-dial code must not be null");
    }
}
