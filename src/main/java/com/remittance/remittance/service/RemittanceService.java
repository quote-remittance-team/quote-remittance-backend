package com.remittance.remittance.service;

import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.entity.Remittance;

import java.util.Optional;

public interface RemittanceService {

    RemittanceResponse createRemittance(
            CreateRemittanceRequest request,
            String userEmail
    );

    RemittanceResponse getByReference(
            String reference,
            String userEmail
    );
}
