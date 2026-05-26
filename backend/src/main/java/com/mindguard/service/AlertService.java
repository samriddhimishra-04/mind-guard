package com.mindguard.service;

import com.mindguard.dto.AlertRequest;
import com.mindguard.dto.AlertResponse;
import com.mindguard.entity.Alert;
import com.mindguard.entity.JournalEntry;
import com.mindguard.repository.AlertRepository;
import com.mindguard.repository.JournalEntryRepository;
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
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    public AlertResponse createAlert(UUID userId, AlertRequest request) {
        UUID journalEntryId = UUID.fromString(request.getJournalEntryId());
        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));

        if (!journalEntry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to create alert for this entry");
        }

        UUID therapistId = null;
        if (request.getAssignedTherapistId() != null && !request.getAssignedTherapistId().isEmpty()) {
            therapistId = UUID.fromString(request.getAssignedTherapistId());
        }

        Alert alert = Alert.builder()
                .userId(userId)
                .journalEntryId(journalEntryId)
                .level(request.getLevel())
                .description(request.getDescription())
                .triggeredBy(request.getTriggeredBy())
                .status(Alert.AlertStatus.NEW)
                .assignedTherapistId(therapistId)
                .build();

        alert = alertRepository.save(alert);
        log.info("Alert created for user {} with level {}", userId, request.getLevel());

        return mapToResponse(alert);
    }

    public AlertResponse getAlert(UUID userId, UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this alert");
        }

        return mapToResponse(alert);
    }

    public List<AlertResponse> getUserAlerts(UUID userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AlertResponse> getUserAlertsByStatus(UUID userId, Alert.AlertStatus status) {
        return alertRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AlertResponse> getCriticalAlerts(UUID userId) {
        return alertRepository.findCriticalUnresolvedAlerts(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AlertResponse> getTherapistAlerts(UUID therapistId) {
        return alertRepository.findUnresolvedAlertsForTherapist(therapistId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AlertResponse updateAlertStatus(UUID userId, UUID alertId, Alert.AlertStatus status) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this alert");
        }

        alert.setStatus(status);

        if (status == Alert.AlertStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
        }

        alert = alertRepository.save(alert);
        log.info("Alert {} status updated to {}", alertId, status);

        return mapToResponse(alert);
    }

    public AlertResponse markAlertAsViewed(UUID userId, UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this alert");
        }

        if (alert.getViewedAt() == null) {
            alert.setViewedAt(LocalDateTime.now());
            alert = alertRepository.save(alert);
        }

        return mapToResponse(alert);
    }

    public void deleteAlert(UUID userId, UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this alert");
        }

        alertRepository.deleteById(alertId);
        log.info("Alert deleted: {}", alertId);
    }

    public void createAlertFromJournalEntry(JournalEntry entry) {
        if (entry.getDistressLevel() != null && entry.getDistressLevel() > 0.7) {
            Alert.AlertLevel level = Alert.AlertLevel.HIGH;
            if (entry.getDistressLevel() > 0.9) {
                level = Alert.AlertLevel.CRITICAL;
            }

            Alert alert = Alert.builder()
                    .userId(entry.getUserId())
                    .journalEntryId(entry.getId())
                    .level(level)
                    .description("High distress detected in journal entry: " + entry.getTitle())
                    .triggeredBy("AI_ANALYSIS")
                    .status(Alert.AlertStatus.NEW)
                    .build();

            alertRepository.save(alert);
            log.warn("Alert created from journal entry {} with level {}", entry.getId(), level);
        }
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId().toString())
                .userId(alert.getUserId().toString())
                .journalEntryId(alert.getJournalEntryId().toString())
                .level(alert.getLevel())
                .description(alert.getDescription())
                .triggeredBy(alert.getTriggeredBy())
                .status(alert.getStatus())
                .assignedTherapistId(alert.getAssignedTherapistId() != null ? alert.getAssignedTherapistId().toString() : null)
                .viewedAt(alert.getViewedAt())
                .resolvedAt(alert.getResolvedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
