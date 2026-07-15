package com.insurance.backend.controller;

import com.insurance.backend.dto.AuditLogDto;
import com.insurance.backend.dto.UserCreateRequest;
import com.insurance.backend.entity.User;
import com.insurance.backend.service.AuditLogService;
import com.insurance.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final UserService userService;

    public AuditLogController(AuditLogService auditLogService, UserService userService) {
        this.auditLogService = auditLogService;
        this.userService = userService;
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

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        User created = userService.createUser(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
