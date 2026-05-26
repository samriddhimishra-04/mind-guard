package com.mindguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    private Integer totalEntries;
    private Integer totalMoodLogs;
    private Double averageSentimentScore;
    private Double averageDistressLevel;
    private Double averageMoodIntensity;
    private String mostFrequentMood;
    private String mostFrequentTrigger;
    private Integer flaggedEntriesCount;
    private Map<String, Integer> moodDistribution;
    private Map<String, Double> weeklyTrend;
}
