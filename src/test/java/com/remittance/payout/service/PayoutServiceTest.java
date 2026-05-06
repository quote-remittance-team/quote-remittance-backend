package com.remittance.payout.service;

import com.remittance.enums.PayoutStatus;
import com.remittance.integration.payout.PayoutClient;
import com.remittance.integration.payout.dto.TransferRequest;
import com.remittance.integration.payout.dto.TransferResponse;
import com.remittance.payout.entity.Payout;
import com.remittance.payout.repository.PayoutRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {
    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private PayoutClient payoutClient;

    @InjectMocks
    private PayoutService payoutService;

    @Test
    void processPayout_WhenPaystackSucceeds_UpdatesStatusToComplete() {
        UUID fakeRemittanceId = UUID.randomUUID();
        String fakeReceiver = "RCP_OLD_TRAFFORD_99";
        long fakeAmount = 5000;
        Payout mockPayout = Payout.builder().build();
        mockPayout.setStatus(PayoutStatus.PENDING);
        when(payoutRepository.findByRemittanceId(fakeRemittanceId)).thenReturn(Optional.of(mockPayout));
        TransferResponse.Data mockData = mock(TransferResponse.Data.class);
        when(mockData.transferCode()).thenReturn("TRF_GGMU_2008");
        TransferResponse successResponse = new TransferResponse(true, "Transfer queued successfully", mockData);
        when(payoutClient.initiateTransfer(any(TransferRequest.class))).thenReturn(successResponse);
        payoutService.processPayout(fakeRemittanceId, fakeReceiver, fakeAmount);
        verify(payoutRepository,times(1)).save(mockPayout);
        assertEquals("TRF_GGMU_2008", mockPayout.getProviderReference());
        assertEquals(PayoutStatus.COMPLETED, mockPayout.getStatus());
    }

    @Test
    void processPayout_WhenPaystackFails_UpdatesStatusToFailed() {
        UUID fakeRemittanceId = UUID.randomUUID();
        Payout mockPayout = Payout.builder().build();
        mockPayout.setStatus(PayoutStatus.PENDING);
        when(payoutRepository.findByRemittanceId(fakeRemittanceId)).thenReturn(Optional.of(mockPayout));
        TransferResponse failureResponse = new TransferResponse(false, "Insufficient balance", null);
        when(payoutClient.initiateTransfer(any(TransferRequest.class))).thenReturn(failureResponse);
        payoutService.processPayout(fakeRemittanceId, "RCP_OLD_TRAFFORD_99", 50000);
        verify(payoutRepository,times(1)).save(mockPayout);
        assertEquals(PayoutStatus.FAILED, mockPayout.getStatus());
    }
}
