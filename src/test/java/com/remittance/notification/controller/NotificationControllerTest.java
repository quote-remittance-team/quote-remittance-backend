package com.remittance.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.notification.dto.NotificationRequestDto;
import com.remittance.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)

public class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void sendNotification_ShouldReturn200Ok_WhenPayloadIsValid() throws Exception {
        NotificationRequestDto validRequest = new NotificationRequestDto();
        validRequest.setUserId(UUID.randomUUID());
        validRequest.setMessage("Your deposit was successfully deposited");
        mockMvc.perform(post("/notifications/send").contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(validRequest))).andExpect(status().isOk());
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    void sendNotification_ShouldReturn400_BadRequest_WhenMessageIsBlack() throws Exception {
        NotificationRequestDto invalidRequest = new NotificationRequestDto();
        invalidRequest.setUserId(UUID.randomUUID());
        mockMvc.perform(post("/notifications/send").contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
        verify(notificationService, times(0)).sendNotification(any(NotificationRequestDto.class));
    }
}
