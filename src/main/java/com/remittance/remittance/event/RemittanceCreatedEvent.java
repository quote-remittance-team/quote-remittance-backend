package com.remittance.remittance.event;

import com.remittance.remittance.entity.Remittance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RemittanceCreatedEvent {

    private final Remittance remittance;
}