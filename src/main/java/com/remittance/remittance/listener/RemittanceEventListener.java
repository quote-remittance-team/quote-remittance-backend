package com.remittance.remittance.listener;

import com.remittance.payout.service.PayoutService;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

        payoutService.triggerPayout(
                event.getRemittance()
        );
    }
}