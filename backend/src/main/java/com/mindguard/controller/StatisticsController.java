package com.mindguard.controller;

import com.mindguard.dto.StatisticsResponse;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/statistics")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/user")
    public ResponseEntity<StatisticsResponse> getUserStatistics(
            @RequestHeader("Authorization") String token) {
        log.info("Get user statistics");
        UUID userId = extractUserIdFromToken(token);
        StatisticsResponse statistics = statisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<StatisticsResponse> getPatientStatistics(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID patientId) {
        log.info("Get patient statistics: {}", patientId);
        UUID therapistId = extractUserIdFromToken(token);
        StatisticsResponse statistics = statisticsService.getPatientStatisticsForTherapist(therapistId, patientId);
        return ResponseEntity.ok(statistics);
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
