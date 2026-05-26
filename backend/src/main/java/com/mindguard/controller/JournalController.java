package com.mindguard.controller;

import com.mindguard.dto.JournalEntryRequest;
import com.mindguard.dto.JournalEntryResponse;
import com.mindguard.security.JwtTokenProvider;
import com.mindguard.service.JournalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/journals")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class JournalController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<JournalEntryResponse> createEntry(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody JournalEntryRequest request) {
        log.info("Create journal entry request");
        UUID userId = extractUserIdFromToken(token);
        JournalEntryResponse response = journalService.createEntry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryResponse> getEntry(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID entryId) {
        log.info("Get journal entry: {}", entryId);
        UUID userId = extractUserIdFromToken(token);
        JournalEntryResponse response = journalService.getEntry(userId, entryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> getUserEntries(
            @RequestHeader("Authorization") String token) {
        log.info("Get user journal entries");
        UUID userId = extractUserIdFromToken(token);
        List<JournalEntryResponse> entries = journalService.getUserEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/range")
    public ResponseEntity<List<JournalEntryResponse>> getEntriesByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        log.info("Get journal entries from date: {}", startDate);
        UUID userId = extractUserIdFromToken(token);
        List<JournalEntryResponse> entries = journalService.getEntriesByDateRange(userId, startDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<JournalEntryResponse>> getFlaggedEntries(
            @RequestHeader("Authorization") String token) {
        log.info("Get flagged journal entries");
        UUID userId = extractUserIdFromToken(token);
        List<JournalEntryResponse> entries = journalService.getFlaggedEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<JournalEntryResponse> updateEntry(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID entryId,
            @Valid @RequestBody JournalEntryRequest request) {
        log.info("Update journal entry: {}", entryId);
        UUID userId = extractUserIdFromToken(token);
        JournalEntryResponse response = journalService.updateEntry(userId, entryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteEntry(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID entryId) {
        log.info("Delete journal entry: {}", entryId);
        UUID userId = extractUserIdFromToken(token);
        journalService.deleteEntry(userId, entryId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
