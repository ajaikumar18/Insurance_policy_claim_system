package com.insurance.backend.service;

import com.insurance.backend.entity.AuditLog;
import com.insurance.backend.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    private AuditLogService auditLogService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditLogService = new AuditLogServiceImpl(auditLogRepository);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testLog_WithAuthenticatedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@ipcms.local");
        when(authentication.getPrincipal()).thenReturn("admin@ipcms.local");

        auditLogService.log("User Onboarded", "testholder@ipcms.local", "Role: Policyholder");

        verify(auditLogRepository, times(1)).save(argThat(log -> 
                "admin@ipcms.local".equals(log.getUserEmail()) &&
                "User Onboarded".equals(log.getAction()) &&
                "testholder@ipcms.local".equals(log.getTarget()) &&
                "Role: Policyholder".equals(log.getDetail())
        ));
    }

    @Test
    void testLog_WithAnonymousUser() {
        when(securityContext.getAuthentication()).thenReturn(null);

        auditLogService.log("System Check", "Database", "Initial seed verified");

        verify(auditLogRepository, times(1)).save(argThat(log -> 
                "SYSTEM".equals(log.getUserEmail()) &&
                "System Check".equals(log.getAction()) &&
                "Database".equals(log.getTarget()) &&
                "Initial seed verified".equals(log.getDetail())
        ));
    }
}
