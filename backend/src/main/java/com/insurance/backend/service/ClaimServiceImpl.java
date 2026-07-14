package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.Claim;
import com.insurance.backend.entity.Policy;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.repository.ClaimRepository;
import com.insurance.backend.repository.PolicyRepository;
import com.insurance.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            PolicyRepository policyRepository,
                            UserRepository userRepository,
                            AuditLogService auditLogService) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public ClaimDto registerFnol(FnolRequest request) {
        // Look up referencing policy
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid policy identification format indicator"));

        // Validate the 14-day claims registration window (Appendix F)
        LocalDate now = LocalDate.now();
        if (request.getIncidentDate().isAfter(now)) {
            throw new IllegalArgumentException("Incident date cannot be in the future");
        }

        long daysBetween = ChronoUnit.DAYS.between(request.getIncidentDate(), now);
        if (daysBetween > 14) {
            throw new IllegalArgumentException("Claims registration window exceeded: incident must be reported within 14 calendar days");
        }

        // Get currently logged-in user reporting the incident
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated to report FNOL");
        }
        String email = auth.getName();
        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Reporter user not found with email: " + email));

        // Generate unique claim number CLM-YYYY-NNNN
        String claimNumber;
        do {
            int randomInt = (int) (Math.random() * 10000);
            claimNumber = "CLM-" + request.getIncidentDate().getYear() + "-" + String.format("%04d", randomInt);
        } while (claimRepository.existsByClaimNumber(claimNumber));

        Claim claim = Claim.builder()
                .claimNumber(claimNumber)
                .policy(policy)
                .reporter(reporter)
                .incidentDate(request.getIncidentDate())
                .incidentType(request.getIncidentType())
                .incidentLocation(request.getIncidentLocation())
                .lossDescription(request.getLossDescription())
                .grossReserve(BigDecimal.ZERO) // Default gross reserve is set to 0.00 as per Appendix B
                .status("Pending FNOL")
                .build();

        Claim saved = claimRepository.save(claim);
        auditLogService.log("FNOL Registered", saved.getClaimNumber(), 
                "Incident date: " + saved.getIncidentDate() + ", Type: " + saved.getIncidentType() + ", Location: " + saved.getIncidentLocation());
        return mapToDto(saved);
    }

    @Override
    public List<ClaimDto> getClaimsByScope() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<Claim> claims;
        if (user.getRole() == Role.POLICYHOLDER) {
            claims = claimRepository.findByPolicyPolicyholderEmail(email);
        } else {
            claims = claimRepository.findAll();
        }

        return claims.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimDto updateReserve(Long id, ReserveUpdateRequest request) {
        // Enforce role permission checks: Modify Reserve Totals is restricted to Claims Handler and Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getRole() != Role.CLAIMS_HANDLER && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only Claims Handlers and Admins can modify reserve totals");
        }

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + id));

        BigDecimal oldReserve = claim.getGrossReserve();
        claim.setGrossReserve(request.getGrossReserve());
        
        // If updating reserve, status typically shifts to Under Investigation
        if ("Pending FNOL".equals(claim.getStatus())) {
            claim.setStatus("Under Investigation");
        }

        Claim saved = claimRepository.save(claim);
        
        auditLogService.log("Reserve Updated", saved.getClaimNumber(), 
                "Gross reserve adjusted: $" + oldReserve + " -> $" + saved.getGrossReserve());
        return mapToDto(saved);
    }

    private ClaimDto mapToDto(Claim claim) {
        String holderName = claim.getPolicy().getPolicyholder().getLegalName() != null ? 
                claim.getPolicy().getPolicyholder().getLegalName() : 
                claim.getPolicy().getPolicyholder().getUsername();

        return ClaimDto.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .policyholderName(holderName)
                .incidentType(claim.getIncidentType())
                .incidentDate(claim.getIncidentDate().toString())
                .incidentLocation(claim.getIncidentLocation())
                .grossReserve(claim.getGrossReserve())
                .lossDescription(claim.getLossDescription())
                .adjuster(claim.getReporter().getRole() == Role.CLAIMS_HANDLER ? claim.getReporter().getLegalName() : "—")
                .status(claim.getStatus())
                .build();
    }
}
