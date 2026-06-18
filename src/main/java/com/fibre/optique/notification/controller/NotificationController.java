package com.fibre.optique.notification.controller;

import com.fibre.optique.notification.dto.NotificationDto;
import com.fibre.optique.notification.dto.SendNotificationRequest;
import com.fibre.optique.notification.entity.Notification;
import com.fibre.optique.notification.service.NotificationService;
import com.fibre.optique.users.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // CLIENT — own in-app notifications
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of notifications for the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                notificationService.getByRecipientPaged(currentUser.getEmail(), pageable));
    }

    // -------------------------------------------------------------------------
    // ADMIN
    // -------------------------------------------------------------------------

    /**
     * Returns all failed notifications — for monitoring / retry.
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getFailed() {
        return ResponseEntity.ok(notificationService.getFailed());
    }

    /**
     * Returns all notifications for a given recipient email — ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getByRecipient(
            @RequestParam String email) {
        return ResponseEntity.ok(notificationService.getByRecipient(email));
    }

    /**
     * Manually sends a notification — admin utility endpoint.
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> send(
            @Valid @RequestBody SendNotificationRequest request) {

        Notification notification = switch (request.getTypeCanal()) {
            case EMAIL -> notificationService.sendEmail(
                    request.getDestinataire(), request.getSujet(), request.getContenu());
            case SMS   -> notificationService.sendSms(
                    request.getDestinataire(), request.getContenu());
            case IN_APP -> notificationService.createInAppNotification(
                    request.getDestinataire(), request.getSujet(), request.getContenu());
        };

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationDto.fromEntity(notification));
    }

    /**
     * Retries all failed email notifications.
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> retryFailed() {
        int retried = notificationService.retryFailedEmails();
        return ResponseEntity.ok(Map.of("emailsRetried", retried));
    }
}
