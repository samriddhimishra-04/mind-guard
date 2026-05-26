package com.mindguard.repository;

import com.mindguard.entity.TherapistPatient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TherapistPatientRepository extends JpaRepository<TherapistPatient, UUID> {
    List<TherapistPatient> findByTherapistId(UUID therapistId);

    Page<TherapistPatient> findByTherapistId(UUID therapistId, Pageable pageable);

    List<TherapistPatient> findByPatientId(UUID patientId);

    List<TherapistPatient> findByTherapistIdAndIsActiveTrue(UUID therapistId);

    Optional<TherapistPatient> findByTherapistIdAndPatientId(UUID therapistId, UUID patientId);

    boolean existsByTherapistIdAndPatientId(UUID therapistId, UUID patientId);

    @Query("SELECT COUNT(tp) FROM TherapistPatient tp WHERE tp.therapistId = :therapistId AND tp.isActive = true")
    int countActivePatients(@Param("therapistId") UUID therapistId);

    @Query("SELECT COUNT(tp) FROM TherapistPatient tp WHERE tp.patientId = :patientId AND tp.isActive = true")
    int countActiveTherapists(@Param("patientId") UUID patientId);

    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapistId = :therapistId AND tp.isActive = true ORDER BY tp.assignedAt DESC")
    List<TherapistPatient> findActiveByTherapistId(@Param("therapistId") UUID therapistId);
}
