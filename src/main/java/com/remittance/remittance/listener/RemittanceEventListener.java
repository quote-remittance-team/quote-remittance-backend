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


@Slf4j
@Component
@RequiredArgsConstructor
public class RemittanceEventListener {

    private final PayoutService payoutService;
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleRemittanceCreated(
            RemittanceCreatedEvent event
    ) {

        var remittance = event.getRemittance();

        log.info(
                "Transaction committed. Creating payout for remittance {}",
                remittance.getReference()
        );

        payoutService.createPayout(remittance);

        log.info(
                "Payout created in PENDING state for remittance {}",
                remittance.getId()
        );


    }
}