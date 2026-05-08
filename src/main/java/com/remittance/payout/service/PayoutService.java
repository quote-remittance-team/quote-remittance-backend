package com.remittance.payout.service;

import com.remittance.enums.PayoutStatus;
import com.remittance.integration.payout.PayoutClient;
import com.remittance.integration.payout.dto.TransferRequest;
import com.remittance.integration.payout.dto.TransferResponse;
import com.remittance.payout.entity.Payout;
import com.remittance.payout.repository.PayoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@Service
public class PayoutService {
    private final PayoutRepository payoutRepository;
    private final PayoutClient payoutClient;

    @Value("${paystack.transfer.source:balance}")
    private String transferSource;

    public PayoutService(PayoutRepository payoutRepository, PayoutClient payoutClient) {
        this.payoutRepository = payoutRepository;
        this.payoutClient = payoutClient;
    }
    @Transactional
    public void processPayout(UUID remittanceId, String recipientCode, long amountInKobo) {
        Payout payout = payoutRepository.findByRemittanceId(remittanceId).orElseThrow(() -> new IllegalArgumentException("No pending payout found for Remittance Id: " + remittanceId));
        if (payout.getStatus() != PayoutStatus.PENDING) {
            log.warn("Payout for remittance {} is not Pending. Current status: {}", remittanceId, payout.getStatus());
            return;
        }

        TransferRequest request = new TransferRequest(
                transferSource,
                amountInKobo,
                recipientCode,
                "Payout for Remittance: " + remittanceId.toString()
        );
        try {
            TransferResponse response = payoutClient.initiateTransfer(request);
            if (response.status()) {
                payout.setProviderReference(response.data().transferCode());
                payout.setStatus(PayoutStatus.COMPLETED);
            } else {
                payout.setStatus(PayoutStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Fatal network error while communicating with paystack for Remttance ID: {}", remittanceId, e);
            payout.setStatus(PayoutStatus.FAILED);
        }

        payoutRepository.save(payout);
    }
}
