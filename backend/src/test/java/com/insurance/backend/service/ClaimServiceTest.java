package com.insurance.backend.service;

import com.insurance.backend.dto.ClaimDto;
import com.insurance.backend.dto.FnolRequest;
import com.insurance.backend.dto.ReserveUpdateRequest;
import com.insurance.backend.entity.Claim;
import com.insurance.backend.entity.Policy;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.repository.ClaimRepository;
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

class ClaimServiceTest {

    private ClaimService claimService;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

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
        claimService = new ClaimServiceImpl(claimRepository, policyRepository, userRepository, auditLogService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testRegisterFnol_Success() {
        FnolRequest request = new FnolRequest();
        request.setPolicyNumber("POL-2024-001");
        request.setIncidentDate(LocalDate.now().minusDays(5));
        request.setIncidentType("Auto Collision");
        request.setIncidentLocation("123 Street");
        request.setLossDescription("Car dented");

        Policy mockPolicy = Policy.builder()
                .policyNumber("POL-2024-001")
                .policyholder(User.builder().username("holder").build())
                .build();

        User mockUser = User.builder().email("reporter@ipcms.local").role(Role.AGENT).build();

        when(policyRepository.findByPolicyNumber("POL-2024-001")).thenReturn(Optional.of(mockPolicy));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("reporter@ipcms.local");
        when(userRepository.findByEmail("reporter@ipcms.local")).thenReturn(Optional.of(mockUser));
        when(claimRepository.existsByClaimNumber(anyString())).thenReturn(false);

        Claim mockClaim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-2024-0001")
                .policy(mockPolicy)
                .reporter(mockUser)
                .incidentDate(request.getIncidentDate())
                .incidentType(request.getIncidentType())
                .incidentLocation(request.getIncidentLocation())
                .lossDescription(request.getLossDescription())
                .grossReserve(BigDecimal.ZERO)
                .status("Pending FNOL")
                .build();

        when(claimRepository.save(any(Claim.class))).thenReturn(mockClaim);

        ClaimDto response = claimService.registerFnol(request);

        assertNotNull(response);
        assertEquals("CLM-2024-0001", response.getClaimNumber());
        assertEquals("Pending FNOL", response.getStatus());
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void testRegisterFnol_WindowExceeded_ThrowsException() {
        FnolRequest request = new FnolRequest();
        request.setPolicyNumber("POL-2024-001");
        request.setIncidentDate(LocalDate.now().minusDays(20)); // > 14 days
        request.setIncidentType("Flood");

        Policy mockPolicy = Policy.builder().policyNumber("POL-2024-001").build();
        when(policyRepository.findByPolicyNumber("POL-2024-001")).thenReturn(Optional.of(mockPolicy));

        assertThrows(IllegalArgumentException.class, () -> claimService.registerFnol(request));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void testUpdateReserve_Success() {
        Long claimId = 1L;
        ReserveUpdateRequest request = new ReserveUpdateRequest();
        request.setGrossReserve(new BigDecimal("15000.00"));

        User mockClaimsHandler = User.builder().email("handler@ipcms.local").role(Role.CLAIMS_HANDLER).build();
        Policy mockPolicy = Policy.builder()
                .policyholder(User.builder().username("holder").build())
                .build();
        Claim mockClaim = Claim.builder()
                .id(claimId)
                .status("Pending FNOL")
                .policy(mockPolicy)
                .reporter(mockClaimsHandler)
                .incidentDate(LocalDate.now())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("handler@ipcms.local");
        when(userRepository.findByEmail("handler@ipcms.local")).thenReturn(Optional.of(mockClaimsHandler));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimDto response = claimService.updateReserve(claimId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("15000.00"), response.getGrossReserve());
        assertEquals("Under Investigation", response.getStatus());
    }

    @Test
    void testUpdateReserve_AccessDenied_ForPolicyholder() {
        Long claimId = 1L;
        ReserveUpdateRequest request = new ReserveUpdateRequest();
        request.setGrossReserve(new BigDecimal("15000.00"));

        User mockHolder = User.builder().email("holder@ipcms.local").role(Role.POLICYHOLDER).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("holder@ipcms.local");
        when(userRepository.findByEmail("holder@ipcms.local")).thenReturn(Optional.of(mockHolder));

        assertThrows(AccessDeniedException.class, () -> claimService.updateReserve(claimId, request));
        verify(claimRepository, never()).save(any(Claim.class));
    }
}
