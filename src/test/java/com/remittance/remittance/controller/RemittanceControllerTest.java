package com.remittance.remittance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.auth.security.JwtAuthenticationFilter;
import com.remittance.auth.security.JwtService;
import com.remittance.remittance.dto.CreateRemittanceRequest;
import com.remittance.remittance.dto.RemittanceResponse;
import com.remittance.remittance.service.RemittanceService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RemittanceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class RemittanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RemittanceService remittanceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createRemittance_ShouldReturn201Created() throws Exception {

        UUID depositId = UUID.randomUUID();

        CreateRemittanceRequest request =
                CreateRemittanceRequest.builder()
                        .depositId(depositId)
                        .receiverName("John Doe")
                        .receiverAccountNumber("1234567890")
                        .receiverBankCode("044")
                        .idempotencyKey("idem-key-123")
                        .build();

        RemittanceResponse response =
                RemittanceResponse.builder()
                        .remittanceId(UUID.randomUUID())
                        .reference("RMT-123ABC")
                        .sendAmount(BigDecimal.valueOf(100))
                        .receiveAmount(BigDecimal.valueOf(150000))
                        .status(
                                com.remittance.enums.RemittanceStatus.PROCESSING
                        )
                        .createdAt(Instant.now())
                        .build();

        when(remittanceService.createRemittance(
                any(CreateRemittanceRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/remittances")
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.reference")
                                .value("RMT-123ABC")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PROCESSING")
                );

        verify(remittanceService, times(1))
                .createRemittance(any(CreateRemittanceRequest.class));
    }

    @Test
    void createRemittance_ShouldReturn400BadRequest_WhenPayloadIsInvalid()
            throws Exception {

        CreateRemittanceRequest request =
                CreateRemittanceRequest.builder()
                        .receiverName("")
                        .receiverAccountNumber("")
                        .receiverBankCode("")
                        .idempotencyKey("")
                        .build();

        mockMvc.perform(
                        post("/remittances")
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(remittanceService, times(0))
                .createRemittance(any(CreateRemittanceRequest.class));
    }

    @Test
    void getByReference_ShouldReturn200Ok() throws Exception {

        String reference = "RMT-123ABC";

        RemittanceResponse response =
                RemittanceResponse.builder()
                        .remittanceId(UUID.randomUUID())
                        .reference(reference)
                        .sendAmount(BigDecimal.valueOf(100))
                        .receiveAmount(BigDecimal.valueOf(150000))
                        .status(
                                com.remittance.enums.RemittanceStatus.PROCESSING
                        )
                        .createdAt(Instant.now())
                        .build();

        when(remittanceService.getByReference(reference))
                .thenReturn(response);

        mockMvc.perform(
                        get("/remittances/{reference}", reference)
                                .with(user("test@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.reference")
                                .value(reference)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PROCESSING")
                );

        verify(remittanceService, times(1))
                .getByReference(reference);
    }
}
