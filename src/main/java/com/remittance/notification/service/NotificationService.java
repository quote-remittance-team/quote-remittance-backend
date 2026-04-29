package com.remittance.notification.service;

import com.remittance.notification.dto.NotificationRequestDto;
import com.remittance.notification.entity.Notification;
import com.remittance.enums.NotificationStatus;
import com.remittance.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {
    private final NotificationRepository notificationRepository;
    public Notification sendNotification(NotificationRequestDto request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);
        try {
            deliverEmailOrSms(request.getUserId(), request.getMessage());
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}", request.getUserId(), e);
            notification.setStatus(NotificationStatus.FAILED);
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED);
        List<Notification> processedNotifications = new java.util.ArrayList<>();
        for (Notification notification : failedNotifications) {
            try {
                deliverEmailOrSms(notification.getUserId(), notification.getMessage());
                notification.setStatus(NotificationStatus.SENT);
            } catch (Exception e) {
                log.error("Retry failed for notification ID: {}", notification.getId());
            }
            processedNotifications.add(notificationRepository.save(notification));
        }
        return processedNotifications;
    }
    private void deliverEmailOrSms(UUID userId, String message) {
        log.info("Simulating dispatch to user {}: {}", userId, message);
    }
}
