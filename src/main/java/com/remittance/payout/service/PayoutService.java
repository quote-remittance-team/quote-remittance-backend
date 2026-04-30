package com.remittance.payout.service;

import com.remittance.remittance.entity.Remittance;

public interface PayoutService {

    void triggerPayout(Remittance remittance);
}
