package com.mindguard.repository;

import com.mindguard.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT je FROM JournalEntry je WHERE je.userId = :userId AND je.createdAt >= :startDate ORDER BY je.createdAt DESC")
    List<JournalEntry> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT je FROM JournalEntry je WHERE je.userId = :userId AND je.isFlagged = true ORDER BY je.createdAt DESC")
    List<JournalEntry> findFlaggedByUserId(@Param("userId") UUID userId);

    List<JournalEntry> findByUserIdAndMood(UUID userId, String mood);
}
