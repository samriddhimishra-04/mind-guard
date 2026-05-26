package com.mindguard.controller;

import com.mindguard.dto.MoodLogRequest;
import com.mindguard.dto.MoodLogResponse;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.MoodService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moods")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class MoodController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<MoodLogResponse> logMood(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody MoodLogRequest request) {
        log.info("Log mood request");
        UUID userId = extractUserIdFromToken(token);
        MoodLogResponse response = moodService.logMood(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{moodLogId}")
    public ResponseEntity<MoodLogResponse> getMoodLog(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID moodLogId) {
        log.info("Get mood log: {}", moodLogId);
        UUID userId = extractUserIdFromToken(token);
        MoodLogResponse response = moodService.getMoodLog(userId, moodLogId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MoodLogResponse>> getUserMoodLogs(
            @RequestHeader("Authorization") String token) {
        log.info("Get user mood logs");
        UUID userId = extractUserIdFromToken(token);
        List<MoodLogResponse> logs = moodService.getUserMoodLogs(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/range")
    public ResponseEntity<List<MoodLogResponse>> getMoodLogsByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        log.info("Get mood logs from date: {}", startDate);
        UUID userId = extractUserIdFromToken(token);
        List<MoodLogResponse> logs = moodService.getMoodLogsByDateRange(userId, startDate);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/type/{mood}")
    public ResponseEntity<List<MoodLogResponse>> getMoodLogsByType(
            @RequestHeader("Authorization") String token,
            @PathVariable String mood) {
        log.info("Get mood logs for mood type: {}", mood);
        UUID userId = extractUserIdFromToken(token);
        List<MoodLogResponse> logs = moodService.getMoodLogsByMoodType(userId, mood);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/range/exact")
    public ResponseEntity<List<MoodLogResponse>> getMoodLogsByExactRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Get mood logs from {} to {}", startDate, endDate);
        UUID userId = extractUserIdFromToken(token);
        List<MoodLogResponse> logs = moodService.getMoodLogsByExactDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/{moodLogId}")
    public ResponseEntity<Void> deleteMoodLog(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID moodLogId) {
        log.info("Delete mood log: {}", moodLogId);
        UUID userId = extractUserIdFromToken(token);
        moodService.deleteMoodLog(userId, moodLogId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
