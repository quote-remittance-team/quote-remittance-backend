package com.remittance.remittance.service;

import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.enums.QuoteStatus;
import com.remittance.enums.RemittanceStatus;
import com.remittance.quote.entity.Quote;
import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.entity.Remittance;
import com.remittance.remittance.event.RemittanceCreatedEvent;
import com.remittance.remittance.repository.RemittanceRepository;
import com.remittance.remittance.service.impl.RemittanceServiceImpl;
import com.remittance.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemittanceServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private RemittanceRepository remittanceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RemittanceServiceImpl remittanceService;

    @Test
    void shouldCreateRemittanceSuccessfully() {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                new CreateRemittanceRequest(
                        depositId,
                        "John Doe",
                        "0123456789",
                        "044",
                        "idem-key-123"
                );

        User user = User.builder().build();

        Quote quote = Quote.builder()
                .quoteReference("QTE-123456")
                .user(user)
                .sendAmount(BigDecimal.valueOf(100))
                .receiveAmount(BigDecimal.valueOf(160000))
                .exchangeRate(BigDecimal.valueOf(1600))
                .status(QuoteStatus.ACTIVE)
                .expiresAt(Instant.now())
                .build();

        Deposit deposit = Deposit.builder()
                .quote(quote)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .status(DepositStatus.CONFIRMED)
                .paymentReference("PAY-123")
                .idempotencyKey("deposit-idem")
                .build();

        ReflectionTestUtils.setField(
                deposit,
                "id",
                depositId
        );

        Remittance savedRemittance = Remittance.builder()
                .reference("RMT-123456")
                .deposit(deposit)
                .quote(quote)
                .sender(user)
                .receiverName("John Doe")
                .receiverAccountNumber("0123456789")
                .receiverBankCode("044")
                .sendAmount(BigDecimal.valueOf(100))
                .receiveAmount(BigDecimal.valueOf(160000))
                .exchangeRate(BigDecimal.valueOf(1600))
                .status(RemittanceStatus.PROCESSING)
                .idempotencyKey("idem-key-123")
                .build();

        ReflectionTestUtils.setField(
                savedRemittance,
                "createdAt",
                Instant.now()
        );

        when(depositRepository.findById(depositId))
                .thenReturn(Optional.of(deposit));

        when(remittanceRepository.findByDepositId(depositId))
                .thenReturn(Optional.empty());

        when(remittanceRepository.findByIdempotencyKey("idem-key-123"))
                .thenReturn(Optional.empty());

        when(remittanceRepository.save(any(Remittance.class)))
                .thenReturn(savedRemittance);

        RemittanceResponse response =
                remittanceService.createRemittance(request);

        assertNotNull(response);

        assertEquals(
                "RMT-123456",
                response.getReference()
        );

        assertEquals(
                RemittanceStatus.PROCESSING,
                response.getStatus()
        );

        verify(remittanceRepository)
                .save(any(Remittance.class));

        ArgumentCaptor<RemittanceCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(RemittanceCreatedEvent.class);

        verify(eventPublisher)
                .publishEvent(eventCaptor.capture());

        RemittanceCreatedEvent publishedEvent =
                eventCaptor.getValue();

        assertNotNull(publishedEvent);

        assertEquals(
                "RMT-123456",
                publishedEvent.getRemittance().getReference()
        );
    }

    @Test
    void shouldThrowExceptionWhenDepositNotFound() {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                new CreateRemittanceRequest(
                        depositId,
                        "John Doe",
                        "0123456789",
                        "044",
                        "idem-key-123"
                );

        when(depositRepository.findById(depositId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> remittanceService.createRemittance(request)
                );

        assertEquals(
                "Deposit not found",
                exception.getMessage()
        );

        verify(remittanceRepository, never())
                .save(any());

        verify(eventPublisher, never())
                .publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenDepositNotConfirmed() {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                new CreateRemittanceRequest(
                        depositId,
                        "John Doe",
                        "0123456789",
                        "044",
                        "idem-key-123"
                );

        Deposit deposit = Deposit.builder()
                .status(DepositStatus.PENDING)
                .build();

        ReflectionTestUtils.setField(
                deposit,
                "id",
                depositId
        );

        when(depositRepository.findById(depositId))
                .thenReturn(Optional.of(deposit));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> remittanceService.createRemittance(request)
                );

        assertEquals(
                "Deposit must be confirmed before remittance",
                exception.getMessage()
        );

        verify(remittanceRepository, never())
                .save(any());

        verify(eventPublisher, never())
                .publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenRemittanceAlreadyExists() {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                new CreateRemittanceRequest(
                        depositId,
                        "John Doe",
                        "0123456789",
                        "044",
                        "idem-key-123"
                );

        Deposit deposit = Deposit.builder()
                .status(DepositStatus.CONFIRMED)
                .build();

        ReflectionTestUtils.setField(
                deposit,
                "id",
                depositId
        );

        Remittance existingRemittance =
                Remittance.builder().build();

        when(depositRepository.findById(depositId))
                .thenReturn(Optional.of(deposit));

        when(remittanceRepository.findByDepositId(depositId))
                .thenReturn(Optional.of(existingRemittance));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> remittanceService.createRemittance(request)
                );

        assertEquals(
                "Remittance already exists for deposit",
                exception.getMessage()
        );

        verify(remittanceRepository, never())
                .save(any());

        verify(eventPublisher, never())
                .publishEvent(any());
    }

    @Test
    void shouldReturnExistingRemittanceForDuplicateIdempotencyKey() {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                new CreateRemittanceRequest(
                        depositId,
                        "John Doe",
                        "0123456789",
                        "044",
                        "idem-key-123"
                );

        User user = User.builder().build();

        Quote quote = Quote.builder()
                .quoteReference("QTE-123456")
                .user(user)
                .sendAmount(BigDecimal.valueOf(100))
                .receiveAmount(BigDecimal.valueOf(160000))
                .exchangeRate(BigDecimal.valueOf(1600))
                .status(QuoteStatus.ACTIVE)
                .expiresAt(Instant.now())
                .build();

        Deposit deposit = Deposit.builder()
                .quote(quote)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .status(DepositStatus.CONFIRMED)
                .paymentReference("PAY-123")
                .idempotencyKey("deposit-idem")
                .build();

        ReflectionTestUtils.setField(
                deposit,
                "id",
                depositId
        );

        Remittance existingRemittance =
                Remittance.builder()
                        .reference("RMT-EXISTING")
                        .deposit(deposit)
                        .quote(quote)
                        .sender(user)
                        .receiverName("John Doe")
                        .receiverAccountNumber("0123456789")
                        .receiverBankCode("044")
                        .sendAmount(BigDecimal.valueOf(100))
                        .receiveAmount(BigDecimal.valueOf(160000))
                        .exchangeRate(BigDecimal.valueOf(1600))
                        .status(RemittanceStatus.PROCESSING)
                        .idempotencyKey("idem-key-123")
                        .build();

        ReflectionTestUtils.setField(
                existingRemittance,
                "createdAt",
                Instant.now()
        );

        when(depositRepository.findById(depositId))
                .thenReturn(Optional.of(deposit));

        when(remittanceRepository.findByDepositId(depositId))
                .thenReturn(Optional.empty());

        when(remittanceRepository.findByIdempotencyKey("idem-key-123"))
                .thenReturn(Optional.of(existingRemittance));

        RemittanceResponse response =
                remittanceService.createRemittance(request);

        assertNotNull(response);

        assertEquals(
                "RMT-EXISTING",
                response.getReference()
        );

        assertEquals(
                RemittanceStatus.PROCESSING,
                response.getStatus()
        );

        verify(remittanceRepository, never())
                .save(any());

        verify(eventPublisher, never())
                .publishEvent(any());
    }
}
