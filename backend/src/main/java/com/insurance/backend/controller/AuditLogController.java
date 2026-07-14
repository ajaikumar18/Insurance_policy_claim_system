package com.insurance.backend.controller;

import com.insurance.backend.dto.AuditLogDto;
import com.insurance.backend.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<AuditLogDto> logs = auditLogService.getAllLogs().stream()
                .map(l -> AuditLogDto.builder()
                        .id(l.getId())
                        .timestamp(l.getTimestamp() != null ? l.getTimestamp().format(formatter) : "")
                        .userEmail(l.getUserEmail())
                        .action(l.getAction())
                        .target(l.getTarget())
                        .detail(l.getDetail())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }
}
