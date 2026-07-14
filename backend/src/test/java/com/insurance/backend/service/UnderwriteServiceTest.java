package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.UnderwriteOverride;
import com.insurance.backend.repository.UnderwriteOverrideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnderwriteServiceTest {

    private UnderwriteService underwriteService;

    @Mock
    private UnderwriteOverrideRepository overrideRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underwriteService = new UnderwriteServiceImpl(overrideRepository, auditLogService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCalculatePremiumQuote_HighRisk() {
        QuoteRequest request = new QuoteRequest();
        request.setRegionScore(80);
        request.setAssetAgeScore(75);
        request.setPriorClaimsScore(90);
        request.setBusinessTypeScore(85);

        QuoteResponse response = underwriteService.calculatePremiumQuote(request);

        assertNotNull(response);
        assertEquals(83, response.getCompositeRiskScore());
        assertEquals("HIGH RISK", response.getRiskLevel());
        // 500.00 + (15.00 * 82.5) = 500 + 1237.5 = 1737.5
        assertEquals(new BigDecimal("1737.50"), response.getPremiumQuote());
    }

    @Test
    void testCalculatePremiumQuote_LowRisk() {
        QuoteRequest request = new QuoteRequest();
        request.setRegionScore(20);
        request.setAssetAgeScore(15);
        request.setPriorClaimsScore(10);
        request.setBusinessTypeScore(25);

        QuoteResponse response = underwriteService.calculatePremiumQuote(request);

        assertNotNull(response);
        assertEquals(18, response.getCompositeRiskScore());
        assertEquals("LOW RISK", response.getRiskLevel());
    }

    @Test
    void testSubmitOverride_Success() {
        OverrideRequest request = new OverrideRequest();
        request.setPolicyNumber("POL-2024-001");
        request.setOverrideType("Rate Reduction");
        request.setDeltaValue("-5%");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("underwriter@ipcms.local");

        UnderwriteOverride mockSaved = UnderwriteOverride.builder()
                .id(1L)
                .policyNumber("POL-2024-001")
                .overrideType("Rate Reduction")
                .deltaValue("-5%")
                .requestedBy("underwriter@ipcms.local")
                .status("PENDING")
                .build();

        when(overrideRepository.save(any(UnderwriteOverride.class))).thenReturn(mockSaved);

        OverrideResponse response = underwriteService.submitOverride(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("underwriter@ipcms.local", response.getRequestedBy());
    }

    @Test
    void testApproveOverride_Success() {
        Long overrideId = 1L;
        UnderwriteOverride mockExisting = UnderwriteOverride.builder()
                .id(overrideId)
                .policyNumber("POL-2024-001")
                .overrideType("Rate Reduction")
                .deltaValue("-5%")
                .requestedBy("underwriter@ipcms.local")
                .status("PENDING")
                .build();

        when(overrideRepository.findById(overrideId)).thenReturn(Optional.of(mockExisting));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("supervisor@ipcms.local");
        when(overrideRepository.save(any(UnderwriteOverride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OverrideResponse response = underwriteService.approveOverride(overrideId);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertEquals("supervisor@ipcms.local", response.getSupervisorSignoffBy());
    }
}
