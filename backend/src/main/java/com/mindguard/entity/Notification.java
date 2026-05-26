package com.mindguard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column
    private UUID relatedEntityId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private DeliveryMethod deliveryMethod;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public enum NotificationType {
        ALERT, ENTRY_FLAGGED, THERAPY_MESSAGE, PATIENT_ASSIGNED, MOOD_PATTERN
    }

    public enum DeliveryMethod {
        IN_APP, EMAIL, PUSH
    }
}
