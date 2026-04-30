package com.remittance.remittance.service.impl;

import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.enums.RemittanceStatus;
import com.remittance.payout.service.PayoutService;
import com.remittance.quote.entity.Quote;
import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.entity.Remittance;
import com.remittance.remittance.repository.RemittanceRepository;
import com.remittance.remittance.service.RemittanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemittanceServiceImpl implements RemittanceService {

    private final DepositRepository depositRepository;
    private final RemittanceRepository remittanceRepository;
    private final PayoutService payoutService;

    @Override
    @Transactional
    public RemittanceResponse createRemittance(
            CreateRemittanceRequest request
    ) {

        Deposit deposit = depositRepository.findById(request.getDepositId())
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        if (deposit.getStatus() != DepositStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Deposit must be confirmed before remittance"
            );
        }

        remittanceRepository.findByDepositId(deposit.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Remittance already exists for deposit"
                    );
                });

        remittanceRepository.findByIdempotencyKey(
                request.getIdempotencyKey())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Duplicate request detected"
                    );
                });

        Quote quote = deposit.getQuote();

        Remittance remittance = Remittance.builder()
                .reference(generateReference())
                .deposit(deposit)
                .quote(quote)
                .sender(quote.getUser())
                .receiverName(request.getReceiverName())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverBankCode(request.getReceiverBankCode())
                .sendAmount(quote.getSendAmount())
                .receiveAmount(quote.getReceiveAmount())
                .exchangeRate(quote.getExchangeRate())
                .status(RemittanceStatus.PROCESSING)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Remittance savedRemittance = remittanceRepository.save(remittance);

        payoutService.triggerPayout(savedRemittance);

        return RemittanceResponse.builder()
                .remittanceId(savedRemittance.getId())
                .reference(savedRemittance.getReference())
                .sendAmount(savedRemittance.getSendAmount())
                .receiveAmount(savedRemittance.getReceiveAmount())
                .status(savedRemittance.getStatus())
                .createdAt(savedRemittance.getCreatedAt())
                .build();
    }

    private  String generateReference() {

        return "RMT-" + UUID.randomUUID()
                .toString()
                .toUpperCase();
    }
}
