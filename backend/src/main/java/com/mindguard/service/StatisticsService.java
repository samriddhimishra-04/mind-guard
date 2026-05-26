package com.mindguard.service;

import com.mindguard.dto.StatisticsResponse;
import com.mindguard.entity.JournalEntry;
import com.mindguard.entity.MoodLog;
import com.mindguard.repository.JournalEntryRepository;
import com.mindguard.repository.MoodLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private MoodLogRepository moodLogRepository;

    public StatisticsResponse getUserStatistics(UUID userId) {
        List<JournalEntry> entries = journalEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<MoodLog> moods = moodLogRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return StatisticsResponse.builder()
                .totalEntries(entries.size())
                .totalMoodLogs(moods.size())
                .averageSentimentScore(calculateAverageSentiment(entries))
                .averageDistressLevel(calculateAverageDistress(entries))
                .averageMoodIntensity(calculateAverageMoodIntensity(moods))
                .mostFrequentMood(findMostFrequentMood(moods))
                .mostFrequentTrigger(findMostFrequentTrigger(moods))
                .flaggedEntriesCount((int) entries.stream().filter(JournalEntry::getIsFlagged).count())
                .moodDistribution(calculateMoodDistribution(moods))
                .weeklyTrend(calculateWeeklyTrend(moods))
                .build();
    }

    public StatisticsResponse getPatientStatisticsForTherapist(UUID therapistId, UUID patientId) {
        // Verify therapist-patient relationship in service would go here
        return getUserStatistics(patientId);
    }

    private Double calculateAverageSentiment(List<JournalEntry> entries) {
        List<Double> sentiments = entries.stream()
                .map(JournalEntry::getSentimentScore)
                .filter(s -> s != null && s > 0)
                .collect(Collectors.toList());

        if (sentiments.isEmpty()) {
            return 0.0;
        }

        return sentiments.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double calculateAverageDistress(List<JournalEntry> entries) {
        List<Double> distresses = entries.stream()
                .map(JournalEntry::getDistressLevel)
                .filter(d -> d != null && d > 0)
                .collect(Collectors.toList());

        if (distresses.isEmpty()) {
            return 0.0;
        }

        return distresses.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double calculateAverageMoodIntensity(List<MoodLog> moods) {
        List<Integer> intensities = moods.stream()
                .map(MoodLog::getIntensityLevel)
                .filter(i -> i != null && i > 0)
                .collect(Collectors.toList());

        if (intensities.isEmpty()) {
            return 0.0;
        }

        return intensities.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
    }

    private String findMostFrequentMood(List<MoodLog> moods) {
        return moods.stream()
                .collect(Collectors.groupingBy(
                        MoodLog::getMood,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private String findMostFrequentTrigger(List<MoodLog> moods) {
        return moods.stream()
                .filter(m -> m.getTriggers() != null && !m.getTriggers().isEmpty())
                .map(MoodLog::getTriggers)
                .flatMap(t -> java.util.Arrays.stream(t.split(",")))
                .map(String::trim)
                .collect(Collectors.groupingBy(
                        s -> s,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None identified");
    }

    private Map<String, Integer> calculateMoodDistribution(List<MoodLog> moods) {
        return moods.stream()
                .collect(Collectors.groupingBy(
                        MoodLog::getMood,
                        Collectors.summingInt(m -> 1)
                ));
    }

    private Map<String, Double> calculateWeeklyTrend(List<MoodLog> moods) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Double> weeklyAverage = new HashMap<>();

        for (int day = 0; day < 7; day++) {
            LocalDateTime dayStart = now.minusDays(day).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            final int dayOfWeek = day;
            Double average = moods.stream()
                    .filter(m -> m.getCreatedAt().isAfter(dayStart) && m.getCreatedAt().isBefore(dayEnd))
                    .mapToDouble(m -> m.getIntensityLevel() != null ? m.getIntensityLevel() : 0)
                    .average()
                    .orElse(0.0);

            String dayName = getDayName(dayOfWeek);
            weeklyAverage.put(dayName, Math.round(average * 100.0) / 100.0);
        }

        return weeklyAverage;
    }

    private String getDayName(int daysAgo) {
        String[] days = {"Today", "Yesterday", "2 days ago", "3 days ago", "4 days ago", "5 days ago", "6 days ago"};
        return days[daysAgo];
    }
}
