package com.mindguard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UUID userId;

    @Column(name = "journal_entry_id", insertable = false, updatable = false)
    private UUID journalEntryId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AlertLevel level;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String triggeredBy;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @Column
    private UUID assignedTherapistId;

    @Column
    private LocalDateTime viewedAt;

    @Column
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", insertable = false, updatable = false)
    private JournalEntry journalEntry;

    public enum AlertLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlertStatus {
        NEW, REVIEWED, ACKNOWLEDGED, RESOLVED, DISMISSED
    }
}
