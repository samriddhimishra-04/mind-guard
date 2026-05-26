package com.mindguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TherapistPatientResponse {
    private String id;
    private String therapistId;
    private String patientId;
    private String patientName;
    private String patientEmail;
    private String notes;
    private Boolean isActive;
    private LocalDateTime assignedAt;
    private LocalDateTime lastInteractionAt;
    private Integer activeAlertsCount;
}
