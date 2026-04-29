package com.remittance.notification.service;

import com.remittance.notification.dto.NotificationRequestDto;
import com.remittance.notification.entity.Notification;
import com.remittance.enums.NotificationStatus;
import com.remittance.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID testUserId;
    private Notification failedNotification;
    private Notification sentNotification;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        failedNotification = Notification.builder()
                .userId(testUserId)
                .message("System Alert")
                .status(NotificationStatus.FAILED)
                .build();

        sentNotification = Notification.builder()
                .userId(testUserId)
                .message("System Alert")
                .status(NotificationStatus.SENT)
                .build();
    }

    @Test
    void sendNotification_ShouldSaveAsSent() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(sentNotification);
        NotificationRequestDto requestDto = new NotificationRequestDto();
        requestDto.setUserId(testUserId);
        requestDto.setMessage("System Alert");
        Notification result = notificationService.sendNotification(requestDto);
        assertEquals(NotificationStatus.SENT, result.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
    @Test
    void retryFailedNotification_ShouldUpdateStatusToSent() {
        when(notificationRepository.findByStatus(NotificationStatus.FAILED)).thenReturn(List.of(failedNotification));
        when(notificationRepository.saveAll(any())).thenReturn(List.of(sentNotification));
        List<Notification> results = notificationService.retryFailedNotifications();
        assertEquals(1, results.size());
        assertEquals(NotificationStatus.SENT, results.get(0).getStatus());
        verify(notificationRepository, times(1)).saveAll(any());
    }
}
