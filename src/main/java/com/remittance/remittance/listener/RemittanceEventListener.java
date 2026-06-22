package com.remittance.remittance.listener;

import com.remittance.payout.service.PayoutService;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemittanceEventListener {

    private final PayoutService payoutService;

    @EventListener
    public void handleRemittanceCreatedSync(RemittanceCreatedEvent event) {
        log.info(
                "Creating payout record synchronously for remittance {}",
                event.getRemittance().getReference()
        );
        payoutService.createPayout(event.getRemittance());
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleRemittanceCreatedAsync(
            RemittanceCreatedEvent event
    ) {
        var remittance = event.getRemittance();

        log.info(
                "Transaction committed. Triggering payout processing for remittance {}",
                remittance.getReference()
        );

        payoutService.processPayout(
                remittance.getId(),
                remittance.getReceiverBankCode(),
                remittance.getReceiveAmount().multiply(BigDecimal.valueOf(100)).longValue()
        );
    }
}