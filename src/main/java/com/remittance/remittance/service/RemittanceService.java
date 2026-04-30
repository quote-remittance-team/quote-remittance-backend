package com.remittance.remittance.service;

import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;

public interface RemittanceService {

    RemittanceResponse createRemittance(CreateRemittanceRequest request);
}

