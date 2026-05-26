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
public class MoodLogResponse {
    private String id;
    private String mood;
    private String notes;
    private Integer intensityLevel;
    private String triggers;
    private LocalDateTime createdAt;
}
