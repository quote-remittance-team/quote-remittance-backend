package com.remittance.remittance.listener;

import com.remittance.payout.service.PayoutService;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Async
@Slf4j
@Component
@RequiredArgsConstructor
public class RemittanceEventListener {

    private final PayoutService payoutService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleRemittanceCreated(
            RemittanceCreatedEvent event
    ) {

        log.info(
                "Transaction committed. Triggering payout for remittance {}",
                event.getRemittance().getReference()
        );

        var remittance = event.getRemittance();

        payoutService.processPayout(
                remittance.getId(),
                remittance.getReceiverBankCode(),
                remittance.getReceiveAmount().multiply(BigDecimal.valueOf(100)).longValue()
        );
    }
}