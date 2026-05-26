package com.mindguard.service;

import com.mindguard.dto.NotificationResponse;
import com.mindguard.entity.Notification;
import com.mindguard.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationResponse createNotification(UUID userId, Notification.NotificationType type, String message, UUID relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .deliveryMethod(Notification.DeliveryMethod.IN_APP)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created for user {} with type {}", userId, type);

        return mapToResponse(notification);
    }

    public void createAlertNotification(UUID userId, UUID alertId, String message) {
        createNotification(userId, Notification.NotificationType.ALERT, message, alertId);
    }

    public void createFlaggedEntryNotification(UUID userId, UUID entryId, String message) {
        createNotification(userId, Notification.NotificationType.ENTRY_FLAGGED, message, entryId);
    }

    public void createPatientAssignedNotification(UUID patientId, UUID therapistId, String message) {
        createNotification(patientId, Notification.NotificationType.PATIENT_ASSIGNED, message, therapistId);
    }

    public NotificationResponse getNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this notification");
        }

        return mapToResponse(notification);
    }

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUserUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUserNotificationsByType(UUID userId, Notification.NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this notification");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }

    public void deleteNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this notification");
        }

        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted: {}", notificationId);
    }

    public int getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId().toString())
                .userId(notification.getUserId().toString())
                .type(notification.getType().toString())
                .message(notification.getMessage())
                .relatedEntityId(notification.getRelatedEntityId() != null ? notification.getRelatedEntityId().toString() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
