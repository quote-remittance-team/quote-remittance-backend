package com.remittance.deposit.service;

import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.dto.DepositResponseDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.repository.DepositRepository;
import com.remittance.enums.DepositStatus;
import com.remittance.integration.payment.PaymentClient;
import com.remittance.integration.payment.dto.PaystackInitializeRequestDto;
import com.remittance.integration.payment.dto.PaystackInitializeResponseDto;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class DepositServiceTest {
    @Mock
    private DepositRepository depositRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private DepositService depositService;

    private Quote mockQuote;
    private Deposit mockDeposit;
    private final String IDEMPOTENCY_KEY = "test-key-123";
    private final String PAYMENT_REFERENCE = "test-ref-456";

    @BeforeEach
    void setUp() {
        mockQuote = Quote.builder()
                    .totalPayable(new BigDecimal("100.00"))
                    .fromCurrency("USD")
                    .build();

        mockDeposit = Deposit.builder()
                .quote(mockQuote)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(DepositStatus.PENDING)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .build();
    }
    // Making a Deposit; test scenario
    @Test
    void initiateDeposit_ShouldCreateAndSaveDeposit() {
        DepositRequestDto requestDto = new DepositRequestDto();
        requestDto.setQuoteId(UUID.randomUUID());
        requestDto.setIdempotencyKey(IDEMPOTENCY_KEY);
        when(quoteRepository.findById(requestDto.getQuoteId())).thenReturn(Optional.of(mockQuote));

        PaystackInitializeResponseDto mockPaystackResponse = new PaystackInitializeResponseDto();
        PaystackInitializeResponseDto.PaystackData mockPaystackData = new PaystackInitializeResponseDto.PaystackData();
        mockPaystackResponse.setStatus(true);
        mockPaystackData.setReference("PAY-12345");
        mockPaystackData.setAuthorisationUrl("hhtps://checkout.paystack.com/fake-url");
        mockPaystackResponse.setData(mockPaystackData);
        when(paymentClient.initializeTransaction(any(PaystackInitializeRequestDto.class))).thenReturn(mockPaystackResponse);

        when(depositRepository.save(any(Deposit.class))).thenReturn(mockDeposit);
        DepositResponseDto result = depositService.initiateDeposit(requestDto, "test@example.com");
        assertNotNull(result);
        assertEquals(DepositStatus.PENDING, result.getStatus());
        verify(paymentClient, times(1)).initializeTransaction(any(PaystackInitializeRequestDto.class));
        verify(depositRepository, times(1)).save(any(Deposit.class));
    }
    //Webhook Success; test scenario
    @Test
    void handlePaymentCallback_ShouldUpdateStatusWhenFound() {
        when(depositRepository.findByPaymentReference(PAYMENT_REFERENCE)).thenReturn(Optional.of(mockDeposit));
        when(depositRepository.save(any(Deposit.class))).thenReturn(mockDeposit);
        Deposit result = depositService.handlePaymentCallback(PAYMENT_REFERENCE, DepositStatus.CONFIRMED);
    }
    //Webhook Failure i.e missing deposit; test scenario
    @Test
    void handlePaymentCallback_ShouldThrowExceptionWhenNotFound() {
        when(depositRepository.findByPaymentReference(PAYMENT_REFERENCE)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> depositService.handlePaymentCallback(PAYMENT_REFERENCE, DepositStatus.CONFIRMED));
        assertTrue(exception.getMessage().contains("Deposit not found"));
        verify(depositRepository, never()).save(any(Deposit.class));
    }

}
