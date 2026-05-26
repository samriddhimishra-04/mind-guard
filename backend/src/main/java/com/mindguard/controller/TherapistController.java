package com.mindguard.controller;

import com.mindguard.dto.*;
import com.mindguard.entity.Alert;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.AlertService;
import com.mindguard.service.JournalService;
import com.mindguard.service.MoodService;
import com.mindguard.service.TherapistService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/therapists")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class TherapistController {

    @Autowired
    private TherapistService therapistService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private MoodService moodService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/patients")
    public ResponseEntity<List<TherapistPatientResponse>> getPatients(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get therapist patients");
        UUID therapistId = extractUserIdFromToken(token);

        if (page == 0 && size == 0) {
            List<TherapistPatientResponse> patients = therapistService.getTherapistPatients(therapistId);
            return ResponseEntity.ok(patients);
        }

        Pageable pageable = PageRequest.of(page, size);
        List<TherapistPatientResponse> patients = therapistService.getTherapistPatientsPage(therapistId, pageable)
                .getContent();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{patientId}/entries")
    public ResponseEntity<List<JournalEntryResponse>> getPatientEntries(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("Get patient entries: {}", patientId);
        UUID therapistId = extractUserIdFromToken(token);

        verifyTherapistPatientRelationship(therapistId, patientId);

        List<JournalEntryResponse> entries = journalService.getUserEntries(patientId)
                .stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        return ResponseEntity.ok(entries);
    }

    @GetMapping("/patients/{patientId}/moods")
    public ResponseEntity<List<MoodLogResponse>> getPatientMoods(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Get patient moods: {}", patientId);
        UUID therapistId = extractUserIdFromToken(token);

        verifyTherapistPatientRelationship(therapistId, patientId);

        List<MoodLogResponse> moods;
        if (startDate != null && endDate != null) {
            moods = moodService.getMoodLogsByExactDateRange(patientId, startDate, endDate);
        } else if (startDate != null) {
            moods = moodService.getMoodLogsByDateRange(patientId, startDate);
        } else {
            moods = moodService.getMoodLogs(patientId);
        }

        return ResponseEntity.ok(moods);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertResponse>> getTherapistAlerts(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Alert.AlertStatus status,
            @RequestParam(required = false) UUID patientId) {
        log.info("Get therapist alerts");
        UUID therapistId = extractUserIdFromToken(token);

        List<AlertResponse> alerts = alertService.getTherapistAlerts(therapistId);

        if (status != null) {
            alerts = alerts.stream()
                    .filter(a -> a.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        if (patientId != null) {
            alerts = alerts.stream()
                    .filter(a -> a.getUserId().equals(patientId.toString()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<AlertResponse> getAlert(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId) {
        log.info("Get alert: {}", alertId);
        UUID therapistId = extractUserIdFromToken(token);

        AlertResponse alert = alertService.getAlert(therapistId, alertId);

        if (!isTherapistForAlert(therapistId, alert)) {
            throw new RuntimeException("Unauthorized to view this alert");
        }

        return ResponseEntity.ok(alert);
    }

    @PostMapping("/patients/{patientId}/assign")
    public ResponseEntity<TherapistPatientResponse> assignPatient(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID patientId,
            @RequestBody(required = false) AssignPatientRequest request) {
        log.info("Assign patient: {}", patientId);
        UUID therapistId = extractUserIdFromToken(token);

        String notes = (request != null && request.getNotes() != null) ? request.getNotes() : "";
        TherapistPatientResponse response = therapistService.assignPatient(therapistId, patientId, notes);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/patients/{patientId}/unassign")
    public ResponseEntity<Void> unassignPatient(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID patientId) {
        log.info("Unassign patient: {}", patientId);
        UUID therapistId = extractUserIdFromToken(token);

        therapistService.unassignPatient(therapistId, patientId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID alertId,
            @Valid @RequestBody ResolveAlertRequest request) {
        log.info("Resolve alert: {}", alertId);
        UUID therapistId = extractUserIdFromToken(token);

        AlertResponse alert = alertService.getAlert(therapistId, alertId);

        if (!isTherapistForAlert(therapistId, alert)) {
            throw new RuntimeException("Unauthorized to resolve this alert");
        }

        AlertResponse updatedAlert = alertService.updateAlertStatus(
                UUID.fromString(alert.getUserId()),
                alertId,
                Alert.AlertStatus.RESOLVED
        );

        return ResponseEntity.ok(updatedAlert);
    }

    private void verifyTherapistPatientRelationship(UUID therapistId, UUID patientId) {
        boolean exists = therapistService.getTherapistPatients(therapistId)
                .stream()
                .anyMatch(tp -> tp.getPatientId().equals(patientId.toString()));

        if (!exists) {
            throw new RuntimeException("Therapist is not assigned to this patient");
        }
    }

    private boolean isTherapistForAlert(UUID therapistId, AlertResponse alert) {
        String assignedTherapistId = alert.getAssignedTherapistId();
        return assignedTherapistId != null && assignedTherapistId.equals(therapistId.toString());
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @GetMapping("/patient-count")
    public ResponseEntity<Integer> getActivePatientCount(
            @RequestHeader("Authorization") String token) {
        UUID therapistId = extractUserIdFromToken(token);
        int count = therapistService.getActivePatientCount(therapistId);
        return ResponseEntity.ok(count);
    }
}
