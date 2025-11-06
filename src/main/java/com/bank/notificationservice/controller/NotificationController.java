package com.bank.notificationservice.controller;

import com.bank.notificationservice.dto.AccountEventNotificationRequest;
import com.bank.notificationservice.dto.AccountStatusChangeNotificationRequest;
import com.bank.notificationservice.dto.HighValueTransactionNotificationRequest;
import com.bank.notificationservice.dto.NotificationResponse;
import com.bank.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/transactions/high-value")
    public ResponseEntity<NotificationResponse> handleHighValueTransaction(
            @Valid @RequestBody HighValueTransactionNotificationRequest request) {
        boolean dispatched = notificationService.handleHighValueTransaction(request);
        if (dispatched) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(NotificationResponse.of("High value transaction notification dispatched."));
        }
        return ResponseEntity.ok(NotificationResponse.of("Transaction below configured threshold; notification skipped."));
    }

    @PostMapping("/accounts/status-change")
    public ResponseEntity<NotificationResponse> handleAccountStatusChange(
            @Valid @RequestBody AccountStatusChangeNotificationRequest request) {
        notificationService.handleAccountStatusChange(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificationResponse.of("Account status change notification dispatched."));
    }

    @PostMapping("/accounts/events")
    public ResponseEntity<NotificationResponse> handleAccountEvents(
            @Valid @RequestBody AccountEventNotificationRequest request) {
        notificationService.handleAccountEvent(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificationResponse.of("Account event notification dispatched."));
    }
}

