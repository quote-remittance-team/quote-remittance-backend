package com.remittance.remittance.notification.repository;

import com.remittance.remittance.notification.entity.Notification;
import com.remittance.remittance.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserId(UUID userId);

    List<Notification> findByStatus(NotificationStatus status);
}
