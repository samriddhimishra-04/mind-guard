package com.mindguard.dto;

import com.mindguard.entity.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private String id;
    private String userId;
    private String journalEntryId;
    private Alert.AlertLevel level;
    private String description;
    private String triggeredBy;
    private Alert.AlertStatus status;
    private String assignedTherapistId;
    private LocalDateTime viewedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
