package com.mindguard.service;

import com.mindguard.dto.TherapistPatientResponse;
import com.mindguard.entity.TherapistPatient;
import com.mindguard.entity.User;
import com.mindguard.repository.AlertRepository;
import com.mindguard.repository.JournalEntryRepository;
import com.mindguard.repository.TherapistPatientRepository;
import com.mindguard.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TherapistService {

    @Autowired
    private TherapistPatientRepository therapistPatientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    public TherapistPatientResponse assignPatient(UUID therapistId, UUID patientId, String notes) {
        User therapist = userRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Therapist not found"));

        if (!therapist.getRole().equals(User.UserRole.THERAPIST)) {
            throw new RuntimeException("User is not a therapist");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!patient.getRole().equals(User.UserRole.PATIENT)) {
            throw new RuntimeException("User is not a patient");
        }

        if (therapistPatientRepository.existsByTherapistIdAndPatientId(therapistId, patientId)) {
            throw new RuntimeException("Patient already assigned to this therapist");
        }

        TherapistPatient assignment = TherapistPatient.builder()
                .therapistId(therapistId)
                .patientId(patientId)
                .notes(notes)
                .isActive(true)
                .build();

        assignment = therapistPatientRepository.save(assignment);
        log.info("Patient {} assigned to therapist {}", patientId, therapistId);

        return mapToResponse(assignment, patient, 0);
    }

    public void unassignPatient(UUID therapistId, UUID patientId) {
        TherapistPatient assignment = therapistPatientRepository.findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setIsActive(false);
        therapistPatientRepository.save(assignment);
        log.info("Patient {} unassigned from therapist {}", patientId, therapistId);
    }

    public List<TherapistPatientResponse> getTherapistPatients(UUID therapistId) {
        return therapistPatientRepository.findByTherapistIdAndIsActiveTrue(therapistId)
                .stream()
                .map(assignment -> {
                    User patient = userRepository.findById(assignment.getPatientId())
                            .orElse(null);
                    int alertCount = alertRepository.findCriticalUnresolvedAlerts(assignment.getPatientId()).size();
                    return mapToResponse(assignment, patient, alertCount);
                })
                .collect(Collectors.toList());
    }

    public Page<TherapistPatientResponse> getTherapistPatientsPage(UUID therapistId, Pageable pageable) {
        return therapistPatientRepository.findByTherapistId(therapistId, pageable)
                .map(assignment -> {
                    User patient = userRepository.findById(assignment.getPatientId())
                            .orElse(null);
                    int alertCount = alertRepository.findCriticalUnresolvedAlerts(assignment.getPatientId()).size();
                    return mapToResponse(assignment, patient, alertCount);
                });
    }

    public List<TherapistPatientResponse> getPatientTherapists(UUID patientId) {
        return therapistPatientRepository.findByPatientId(patientId)
                .stream()
                .filter(TherapistPatient::getIsActive)
                .map(assignment -> {
                    User therapist = userRepository.findById(assignment.getTherapistId())
                            .orElse(null);
                    return mapToResponse(assignment, therapist, 0);
                })
                .collect(Collectors.toList());
    }

    public void updateAssignmentNotes(UUID therapistId, UUID patientId, String notes) {
        TherapistPatient assignment = therapistPatientRepository.findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setNotes(notes);
        therapistPatientRepository.save(assignment);
        log.info("Assignment notes updated for therapist {} and patient {}", therapistId, patientId);
    }

    private TherapistPatientResponse mapToResponse(TherapistPatient assignment, User user, int alertCount) {
        return TherapistPatientResponse.builder()
                .id(assignment.getId().toString())
                .therapistId(assignment.getTherapistId().toString())
                .patientId(assignment.getPatientId().toString())
                .patientName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown")
                .patientEmail(user != null ? user.getEmail() : "")
                .notes(assignment.getNotes())
                .isActive(assignment.getIsActive())
                .assignedAt(assignment.getAssignedAt())
                .activeAlertsCount(alertCount)
                .build();
    }

    public int getActivePatientCount(UUID therapistId) {
        return therapistPatientRepository.countActivePatients(therapistId);
    }

    public int getActiveTherapistCount(UUID patientId) {
        return therapistPatientRepository.countActiveTherapists(patientId);
    }
}
