package com.mindguard.dto;

import com.mindguard.entity.Alert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRequest {
    @NotNull(message = "Journal entry ID is required")
    private String journalEntryId;

    @NotNull(message = "Alert level is required")
    private Alert.AlertLevel level;

    @NotBlank(message = "Description is required")
    private String description;

    private String triggeredBy;

    private String assignedTherapistId;
}
