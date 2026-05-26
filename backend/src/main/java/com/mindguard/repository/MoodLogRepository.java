package com.mindguard.repository;

import com.mindguard.entity.MoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, UUID> {
    List<MoodLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT ml FROM MoodLog ml WHERE ml.userId = :userId AND ml.createdAt >= :startDate ORDER BY ml.createdAt DESC")
    List<MoodLog> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate);

    List<MoodLog> findByUserIdAndMood(UUID userId, String mood);

    @Query("SELECT ml FROM MoodLog ml WHERE ml.userId = :userId AND ml.createdAt >= :startDate AND ml.createdAt <= :endDate ORDER BY ml.createdAt DESC")
    List<MoodLog> findByUserIdAndDateRangeExact(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
