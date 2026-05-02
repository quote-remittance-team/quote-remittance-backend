package com.remittance.integration.payment;

import com.remittance.integration.payment.dto.PaystackInitializeRequestDto;
import com.remittance.integration.payment.dto.PaystackInitializeResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentClientTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks
    private PaymentClient paymentClient;

    @Test
    void initializeTransaction_ShouldReturnPaystackResponse_WhenSuccessful() {
        PaystackInitializeRequestDto requestDto = PaystackInitializeRequestDto.builder()
                .email("test@example.com")
                .amount("10000")
                .reference("REF-123")
                .build();

        PaystackInitializeResponseDto expectedResponse = new PaystackInitializeResponseDto();
        expectedResponse.setStatus(true);
        expectedResponse.setMessage("Authorization URL created");
        PaystackInitializeResponseDto.PaystackData data = new PaystackInitializeResponseDto.PaystackData();
        data.setAuthorisationUrl("https://checkout.paystack.com/fake-url");
        data.setReference("REF-123");
        data.setAccessCode("ACCESS_123");
        expectedResponse.setData(data);

        when(restClient.post().uri(anyString()).contentType(MediaType.APPLICATION_JSON).body(any(PaystackInitializeRequestDto.class)).retrieve().body(eq(PaystackInitializeResponseDto.class))).thenReturn(expectedResponse);
        PaystackInitializeResponseDto actualResponse = paymentClient.initializeTransaction(requestDto);
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isStatus());
        assertEquals("https://checkout.paystack.com/fake-url", actualResponse.getData().getAuthorisationUrl());
        assertEquals("REF-123", actualResponse.getData().getReference());
    }

    @Test
    void initializeTransaction_ShouldThrowException_WhenApiFails() {
        PaystackInitializeRequestDto requestDto = PaystackInitializeRequestDto.builder()
                .email("test@example.com")
                .amount("10000")
                .build();
        when(restClient.post().uri(anyString()).contentType(MediaType.APPLICATION_JSON).body(any(PaystackInitializeRequestDto.class))).thenThrow(new RestClientException("Connection Timeout"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> paymentClient.initializeTransaction(requestDto));
        assertTrue(exception.getMessage().contains("Payment gateway integration failed"));
    }
}
