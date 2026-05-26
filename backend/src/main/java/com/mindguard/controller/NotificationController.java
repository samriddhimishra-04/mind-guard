package com.mindguard.controller;

import com.mindguard.dto.NotificationResponse;
import com.mindguard.entity.Notification;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @RequestHeader("Authorization") String token) {
        log.info("Get user notifications");
        UUID userId = extractUserIdFromToken(token);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @RequestHeader("Authorization") String token) {
        log.info("Get unread notifications");
        UUID userId = extractUserIdFromToken(token);
        List<NotificationResponse> notifications = notificationService.getUserUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestHeader("Authorization") String token) {
        log.info("Get unread notification count");
        UUID userId = extractUserIdFromToken(token);
        int count = notificationService.getUnreadCount(userId);

        Map<String, Integer> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String type) {
        log.info("Get notifications by type: {}", type);
        UUID userId = extractUserIdFromToken(token);

        Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type);
        List<NotificationResponse> notifications = notificationService.getUserNotificationsByType(userId, notificationType);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID notificationId) {
        log.info("Get notification: {}", notificationId);
        UUID userId = extractUserIdFromToken(token);
        NotificationResponse notification = notificationService.getNotification(userId, notificationId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID notificationId) {
        log.info("Mark notification as read: {}", notificationId);
        UUID userId = extractUserIdFromToken(token);
        NotificationResponse notification = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @RequestHeader("Authorization") String token) {
        log.info("Mark all notifications as read");
        UUID userId = extractUserIdFromToken(token);
        notificationService.markAllAsRead(userId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID notificationId) {
        log.info("Delete notification: {}", notificationId);
        UUID userId = extractUserIdFromToken(token);
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
