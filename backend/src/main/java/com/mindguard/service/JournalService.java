package com.mindguard.service;

import com.mindguard.dto.JournalEntryRequest;
import com.mindguard.dto.JournalEntryResponse;
import com.mindguard.entity.JournalEntry;
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
public class JournalService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private AlertService alertService;

    public JournalEntryResponse createEntry(UUID userId, JournalEntryRequest request) {
        JournalEntry entry = JournalEntry.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .tags(request.getTags())
                .build();

        entry = journalEntryRepository.save(entry);
        log.info("Journal entry created for user: {} with id: {}", userId, entry.getId());

        performAiAnalysis(entry);

        return mapToResponse(entry);
    }

    public JournalEntryResponse updateEntry(UUID userId, UUID entryId, JournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this entry");
        }

        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setMood(request.getMood());
        entry.setTags(request.getTags());

        entry = journalEntryRepository.save(entry);
        log.info("Journal entry updated: {}", entryId);

        return mapToResponse(entry);
    }

    public JournalEntryResponse getEntry(UUID userId, UUID entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this entry");
        }

        return mapToResponse(entry);
    }

    public List<JournalEntryResponse> getUserEntries(UUID userId) {
        return journalEntryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<JournalEntryResponse> getEntriesByDateRange(UUID userId, LocalDateTime startDate) {
        return journalEntryRepository.findByUserIdAndDateRange(userId, startDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<JournalEntryResponse> getFlaggedEntries(UUID userId) {
        return journalEntryRepository.findFlaggedByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteEntry(UUID userId, UUID entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this entry");
        }

        journalEntryRepository.deleteById(entryId);
        log.info("Journal entry deleted: {}", entryId);
    }

    private void performAiAnalysis(JournalEntry entry) {
        try {
            aiAnalysisService.analyzeEntry(entry);
            journalEntryRepository.save(entry);
            alertService.createAlertFromJournalEntry(entry);
        } catch (Exception e) {
            log.error("AI analysis failed for entry: {}", entry.getId(), e);
        }
    }

    private JournalEntryResponse mapToResponse(JournalEntry entry) {
        return JournalEntryResponse.builder()
                .id(entry.getId().toString())
                .title(entry.getTitle())
                .content(entry.getContent())
                .mood(entry.getMood())
                .sentimentScore(entry.getSentimentScore())
                .distressLevel(entry.getDistressLevel())
                .aiAnalysis(entry.getAiAnalysis())
                .isFlagged(entry.getIsFlagged())
                .tags(entry.getTags())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
