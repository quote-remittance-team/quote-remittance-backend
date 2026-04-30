package com.remittance.payout.service.impl;

import com.remittance.payout.service.PayoutService;
import com.remittance.remittance.entity.Remittance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PayoutServiceImpl implements PayoutService {

    @Override
    public void triggerPayout(Remittance remittance) {

        log.info(
                "Triggering payout for remittance reference: {}",
                remittance.getReference()
        );
    }
}
