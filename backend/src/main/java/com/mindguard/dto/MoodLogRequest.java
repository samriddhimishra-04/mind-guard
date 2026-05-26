package com.mindguard.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodLogRequest {
    @NotBlank(message = "Mood is required")
    @Size(max = 50, message = "Mood must not exceed 50 characters")
    private String mood;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Min(value = 1, message = "Intensity level must be at least 1")
    @Max(value = 10, message = "Intensity level must not exceed 10")
    private Integer intensityLevel;

    @Size(max = 500, message = "Triggers must not exceed 500 characters")
    private String triggers;
}
