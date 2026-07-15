package com.insurance.backend.service;

import com.insurance.backend.dto.EndorsementRequest;
import com.insurance.backend.dto.EndorsementResponse;
import com.insurance.backend.dto.PolicyCreateRequest;
import com.insurance.backend.entity.Policy;
import com.insurance.backend.entity.PolicyHistory;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.repository.PolicyHistoryRepository;
import com.insurance.backend.repository.PolicyRepository;
import com.insurance.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PolicyEndorsementTest {

    private PolicyService policyService;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyHistoryRepository policyHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        policyService = new PolicyServiceImpl(policyRepository, policyHistoryRepository, userRepository, auditLogService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testEndorsePolicy_Success() {
        Long policyId = 1L;
        EndorsementRequest request = new EndorsementRequest();
        request.setProductType("Marine Cargo");
        request.setBasePremium(new BigDecimal("1200.00"));
        request.setExpiryDate(LocalDate.now().plusDays(100));

        User mockUnderwriter = User.builder().email("underwriter@ipcms.local").role(Role.UNDERWRITER).build();
        Policy mockPolicy = Policy.builder()
                .id(policyId)
                .policyNumber("POL-2024-001")
                .productType("Auto Fleet")
                .basePremium(new BigDecimal("1000.00")) // old base premium
                .expiryDate(LocalDate.now().plusDays(100))
                .status("Active")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("underwriter@ipcms.local");
        when(userRepository.findByEmail("underwriter@ipcms.local")).thenReturn(Optional.of(mockUnderwriter));
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(mockPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EndorsementResponse response = policyService.endorsePolicy(policyId, request);

        assertNotNull(response);
        assertEquals("POL-2024-001", response.getPolicyNumber());
        assertEquals(new BigDecimal("1000.00"), response.getOldPremium());
        assertEquals(new BigDecimal("1200.00"), response.getNewPremium());
        
        // pro-rata adjustment: (1200 - 1000) * (100 / 365.0) = 200 * 0.27397 = 54.79
        assertEquals(new BigDecimal("54.79"), response.getProRataAdjustment());
        assertEquals(100, response.getDaysRemaining());

        verify(policyHistoryRepository, times(1)).save(any(PolicyHistory.class));
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testEndorsePolicy_AccessDenied_ForAgent() {
        Long policyId = 1L;
        EndorsementRequest request = new EndorsementRequest();
        request.setProductType("Marine Cargo");
        request.setBasePremium(new BigDecimal("1200.00"));
        request.setExpiryDate(LocalDate.now().plusDays(100));

        User mockAgent = User.builder().email("agent@ipcms.local").role(Role.AGENT).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("agent@ipcms.local");
        when(userRepository.findByEmail("agent@ipcms.local")).thenReturn(Optional.of(mockAgent));

        assertThrows(AccessDeniedException.class, () -> policyService.endorsePolicy(policyId, request));
        verify(policyHistoryRepository, never()).save(any(PolicyHistory.class));
    }

    @Test
    void testCreatePolicy_Success() {
        PolicyCreateRequest request = PolicyCreateRequest.builder()
                .policyNumber("POL-2026-999")
                .policyholderEmail("holder@ipcms.local")
                .productType("Cyber Liability")
                .basePremium(new BigDecimal("5000.00"))
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        User mockUnderwriter = User.builder().email("underwriter@ipcms.local").role(Role.UNDERWRITER).build();
        User mockHolder = User.builder().email("holder@ipcms.local").role(Role.POLICYHOLDER).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("underwriter@ipcms.local");
        when(userRepository.findByEmail("underwriter@ipcms.local")).thenReturn(Optional.of(mockUnderwriter));
        when(userRepository.findByEmail("holder@ipcms.local")).thenReturn(Optional.of(mockHolder));
        when(policyRepository.existsByPolicyNumber("POL-2026-999")).thenReturn(false);
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Policy created = policyService.createPolicy(request);

        assertNotNull(created);
        assertEquals("POL-2026-999", created.getPolicyNumber());
        assertEquals("Cyber Liability", created.getProductType());
        assertEquals(new BigDecimal("5000.00"), created.getBasePremium());
        assertEquals(mockHolder, created.getPolicyholder());
        verify(policyRepository, times(1)).save(any(Policy.class));
        verify(auditLogService, times(1)).log(eq("Policy Created"), eq("POL-2026-999"), anyString());
    }

    @Test
    void testCreatePolicy_AccessDenied_ForPolicyholder() {
        PolicyCreateRequest request = PolicyCreateRequest.builder()
                .policyNumber("POL-2026-999")
                .policyholderEmail("holder@ipcms.local")
                .productType("Cyber Liability")
                .basePremium(new BigDecimal("5000.00"))
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        User mockHolder = User.builder().email("holder@ipcms.local").role(Role.POLICYHOLDER).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("holder@ipcms.local");
        when(userRepository.findByEmail("holder@ipcms.local")).thenReturn(Optional.of(mockHolder));

        assertThrows(AccessDeniedException.class, () -> policyService.createPolicy(request));
        verify(policyRepository, never()).save(any(Policy.class));
    }
}
