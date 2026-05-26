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
public class JournalEntryResponse {
    private String id;
    private String title;
    private String content;
    private String mood;
    private Double sentimentScore;
    private Double distressLevel;
    private String aiAnalysis;
    private Boolean isFlagged;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
