package com.remittance.payout.service;

import com.remittance.enums.PayoutStatus;
import com.remittance.integration.payout.PayoutClient;
import com.remittance.integration.payout.dto.TransferRequest;
import com.remittance.integration.payout.dto.TransferResponse;
import com.remittance.payout.entity.Payout;
import com.remittance.payout.repository.PayoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PayoutService {
    private final PayoutRepository payoutRepository;
    private final PayoutClient payoutClient;

    public PayoutService(PayoutRepository payoutRepository, PayoutClient payoutClient) {
        this.payoutRepository = payoutRepository;
        this.payoutClient = payoutClient;
    }
    @Transactional
    public void processPayout(UUID remittanceId, String recipientCode, long amountInKobo) {
        Payout payout = payoutRepository.findByRemittanceId(remittanceId).orElseThrow(() -> new IllegalArgumentException("No pending payout found for Remittance Id: " + remittanceId));
        TransferRequest request = new TransferRequest(
                "balance",
                amountInKobo,
                recipientCode,
                "Payout for Remittance: " + remittanceId.toString()
        );
        TransferResponse response = payoutClient.initiateTransfer(request);
        if (response.status()) {
            payout.setProviderReference(response.data().transferCode());
            payout.setStatus(PayoutStatus.COMPLETED);
        } else {
            payout.setStatus(PayoutStatus.FAILED);
        }
        payoutRepository.save(payout);
    }
}
