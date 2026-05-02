package com.remittance.deposit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.deposit.dto.DepositRequestDto;
import com.remittance.deposit.dto.DepositResponseDto;
import com.remittance.deposit.dto.DepositWebhookDto;
import com.remittance.deposit.entity.Deposit;
import com.remittance.deposit.service.DepositService;
import com.remittance.enums.DepositStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = DepositController.class,
        properties = {"remittance.webhook.secret=dummy-test-secret"}
        )
@AutoConfigureMockMvc(addFilters = false)
public class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepositService depositService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private Deposit mockDeposit;

    private DepositResponseDto mockDepositResponseDto;

    @BeforeEach
    void setUp() {
         mockDeposit = Deposit.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(DepositStatus.PENDING)
                .paymentReference("REF123")
                .idempotencyKey("idem-key-123")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(mockDeposit, "id", java.util.UUID.randomUUID());

        mockDepositResponseDto = DepositResponseDto.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(DepositStatus.PENDING)
                .checkoutUrl("https://checkout.paystack.com/fake-url")
                .build();
    }
    @Test
    @WithMockUser
    void createDeposit_ShouldReturn201Created() throws Exception {
        DepositRequestDto requestDto = new DepositRequestDto();
        requestDto.setQuoteId(UUID.randomUUID());
        when(depositService.initiateDeposit(any(DepositRequestDto.class))).thenReturn(mockDepositResponseDto);
        mockMvc.perform(post("/deposits")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockDepositResponseDto.getId().toString()))
                .andExpect(jsonPath("$.amount").value(mockDepositResponseDto.getAmount().doubleValue()))
                .andExpect(jsonPath("$.currency").value(mockDepositResponseDto.getCurrency()))
                .andExpect(jsonPath("$.status").value(mockDepositResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$.checkoutUrl").value(mockDepositResponseDto.getCheckoutUrl()));
        verify(depositService, times(1)).initiateDeposit(any(DepositRequestDto.class));
    }

    @Test
    @WithMockUser
    void handleWebhook_ShouldReturn200Ok() throws Exception {
        DepositWebhookDto webhookDto = new DepositWebhookDto();
        webhookDto.setPaymentReference("PAY-12345");
        webhookDto.setStatus(DepositStatus.CONFIRMED);
        when(depositService.handlePaymentCallback(anyString(),any())).thenReturn(mockDeposit);
        mockMvc.perform(post("/deposits/webhook").header("X-Provider-Signature", "dummy-test-secret").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(webhookDto))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(mockDeposit.getId().toString()));
        verify(depositService, times(1)).handlePaymentCallback(anyString(),any());
    }
}
