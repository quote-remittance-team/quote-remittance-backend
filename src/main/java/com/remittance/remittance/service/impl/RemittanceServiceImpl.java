package com.remittance.remittance.service.impl;

import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.enums.RemittanceStatus;
import com.remittance.quote.entity.Quote;
import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.entity.Remittance;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import com.remittance.remittance.repository.RemittanceRepository;
import com.remittance.remittance.service.RemittanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemittanceServiceImpl implements RemittanceService {

    private final DepositRepository depositRepository;
    private final RemittanceRepository remittanceRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        Optional<Remittance> existingRemittance =
                remittanceRepository.findByIdempotencyKey(
                        request.getIdempotencyKey()
                );
        if (existingRemittance.isPresent()) {

            return mapToResponse(existingRemittance.get());
        }

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

        eventPublisher.publishEvent(
                new RemittanceCreatedEvent(savedRemittance)
        );

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

    private RemittanceResponse mapToResponse(
            Remittance remittance
    ) {

        return RemittanceResponse.builder()
                .reference(remittance.getReference())
                .status(remittance.getStatus())
                .sendAmount(remittance.getSendAmount())
                .receiveAmount(remittance.getReceiveAmount())
                .createdAt(remittance.getCreatedAt())
                .build();
    }
}
