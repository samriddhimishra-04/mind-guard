package com.mindguard.controller;

import com.mindguard.dto.AlertRequest;
import com.mindguard.dto.AlertResponse;
import com.mindguard.entity.Alert;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.AlertService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AlertRequest request) {
        log.info("Create alert request");
        UUID userId = extractUserIdFromToken(token);
        AlertResponse response = alertService.createAlert(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResponse> getAlert(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId) {
        log.info("Get alert: {}", alertId);
        UUID userId = extractUserIdFromToken(token);
        AlertResponse response = alertService.getAlert(userId, alertId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getUserAlerts(
            @RequestHeader("Authorization") String token) {
        log.info("Get user alerts");
        UUID userId = extractUserIdFromToken(token);
        List<AlertResponse> alerts = alertService.getUserAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlertResponse>> getUserAlertsByStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Alert.AlertStatus status) {
        log.info("Get alerts by status: {}", status);
        UUID userId = extractUserIdFromToken(token);
        List<AlertResponse> alerts = alertService.getUserAlertsByStatus(userId, status);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/critical")
    public ResponseEntity<List<AlertResponse>> getCriticalAlerts(
            @RequestHeader("Authorization") String token) {
        log.info("Get critical alerts");
        UUID userId = extractUserIdFromToken(token);
        List<AlertResponse> alerts = alertService.getCriticalAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{alertId}/status/{status}")
    public ResponseEntity<AlertResponse> updateAlertStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId,
            @PathVariable Alert.AlertStatus status) {
        log.info("Update alert status: {} to {}", alertId, status);
        UUID userId = extractUserIdFromToken(token);
        AlertResponse response = alertService.updateAlertStatus(userId, alertId, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{alertId}/view")
    public ResponseEntity<AlertResponse> markAlertAsViewed(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId) {
        log.info("Mark alert as viewed: {}", alertId);
        UUID userId = extractUserIdFromToken(token);
        AlertResponse response = alertService.markAlertAsViewed(userId, alertId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId) {
        log.info("Delete alert: {}", alertId);
        UUID userId = extractUserIdFromToken(token);
        alertService.deleteAlert(userId, alertId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
