package com.mindguard.repository;

import com.mindguard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Alert> findByUserIdAndStatus(UUID userId, Alert.AlertStatus status);

    @Query("SELECT a FROM Alert a WHERE a.userId = :userId AND a.level IN ('HIGH', 'CRITICAL') AND a.status != 'RESOLVED' ORDER BY a.createdAt DESC")
    List<Alert> findCriticalUnresolvedAlerts(@Param("userId") UUID userId);

    @Query("SELECT a FROM Alert a WHERE a.assignedTherapistId = :therapistId AND a.status != 'RESOLVED' ORDER BY a.level DESC, a.createdAt DESC")
    List<Alert> findUnresolvedAlertsForTherapist(@Param("therapistId") UUID therapistId);

    List<Alert> findByJournalEntryId(UUID journalEntryId);
}
