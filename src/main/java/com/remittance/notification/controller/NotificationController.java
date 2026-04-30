package com.remittance.notification.controller;

import com.remittance.notification.dto.NotificationRequestDto;
import com.remittance.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j

public class NotificationController {
    private final NotificationService notificationService;

    //POST /notification/send
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationRequestDto request) {
        log.info("Received request to send notification to user ID: {}", request.getUserId());
        notificationService.sendNotification(request);
        log.info("Notification request processed successfully");
        return ResponseEntity.status(HttpStatus.OK).body("Notification triggered successfully");
    }
}
