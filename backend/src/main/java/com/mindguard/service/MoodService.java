package com.mindguard.service;

import com.mindguard.dto.MoodLogRequest;
import com.mindguard.dto.MoodLogResponse;
import com.mindguard.entity.MoodLog;
import com.mindguard.repository.MoodLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MoodService {

    @Autowired
    private MoodLogRepository moodLogRepository;

    public MoodLogResponse logMood(UUID userId, MoodLogRequest request) {
        MoodLog moodLog = MoodLog.builder()
                .userId(userId)
                .mood(request.getMood())
                .notes(request.getNotes())
                .intensityLevel(request.getIntensityLevel())
                .triggers(request.getTriggers())
                .build();

        moodLog = moodLogRepository.save(moodLog);
        log.info("Mood logged for user: {} with mood: {}", userId, request.getMood());

        return mapToResponse(moodLog);
    }

    public MoodLogResponse getMoodLog(UUID userId, UUID moodLogId) {
        MoodLog moodLog = moodLogRepository.findById(moodLogId)
                .orElseThrow(() -> new RuntimeException("Mood log not found"));

        if (!moodLog.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this mood log");
        }

        return mapToResponse(moodLog);
    }

    public List<MoodLogResponse> getUserMoodLogs(UUID userId) {
        return moodLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MoodLogResponse> getMoodLogs(UUID userId) {
        return getUserMoodLogs(userId);
    }

    public List<MoodLogResponse> getMoodLogsByDateRange(UUID userId, LocalDateTime startDate) {
        return moodLogRepository.findByUserIdAndDateRange(userId, startDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MoodLogResponse> getMoodLogsByMoodType(UUID userId, String mood) {
        return moodLogRepository.findByUserIdAndMood(userId, mood)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MoodLogResponse> getMoodLogsByExactDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return moodLogRepository.findByUserIdAndDateRangeExact(userId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteMoodLog(UUID userId, UUID moodLogId) {
        MoodLog moodLog = moodLogRepository.findById(moodLogId)
                .orElseThrow(() -> new RuntimeException("Mood log not found"));

        if (!moodLog.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this mood log");
        }

        moodLogRepository.deleteById(moodLogId);
        log.info("Mood log deleted: {}", moodLogId);
    }

    private MoodLogResponse mapToResponse(MoodLog moodLog) {
        return MoodLogResponse.builder()
                .id(moodLog.getId().toString())
                .mood(moodLog.getMood())
                .notes(moodLog.getNotes())
                .intensityLevel(moodLog.getIntensityLevel())
                .triggers(moodLog.getTriggers())
                .createdAt(moodLog.getCreatedAt())
                .build();
    }
}
