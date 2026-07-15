package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.Policy;
import com.insurance.backend.entity.PolicyHistory;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.repository.PolicyRepository;
import com.insurance.backend.repository.PolicyHistoryRepository;
import com.insurance.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyHistoryRepository policyHistoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PolicyServiceImpl(PolicyRepository policyRepository,
                             PolicyHistoryRepository policyHistoryRepository,
                             UserRepository userRepository,
                             AuditLogService auditLogService) {
        this.policyRepository = policyRepository;
        this.policyHistoryRepository = policyHistoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Policy> getPoliciesByScope() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getRole() == Role.POLICYHOLDER) {
            return policyRepository.findByPolicyholderEmail(email);
        } else {
            return policyRepository.findAll();
        }
    }

    @Override
    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with id: " + id));
    }

    @Override
    @Transactional
    public EndorsementResponse endorsePolicy(Long id, EndorsementRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated");
        }

        String email = auth.getName();
        User modifier = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Enforce endorsement roles: restricted to Underwriter and Admin
        if (modifier.getRole() != Role.UNDERWRITER && modifier.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only Underwriters and Admins can submit mid-term policy modifications");
        }

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));

        BigDecimal oldPremium = policy.getBasePremium();
        BigDecimal newPremium = request.getBasePremium();
        BigDecimal premiumDiff = newPremium.subtract(oldPremium);

        // 1. Calculate remaining days in active term for pro-rata adjustment
        LocalDate now = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(now, policy.getExpiryDate());
        if (daysRemaining < 0) {
            daysRemaining = 0;
        }

        // Pro-rata premium adjustment formula: diff * (daysRemaining / 365.0)
        BigDecimal proRataAdjustment = BigDecimal.ZERO;
        if (daysRemaining > 0) {
            proRataAdjustment = premiumDiff
                    .multiply(BigDecimal.valueOf(daysRemaining))
                    .divide(BigDecimal.valueOf(365.0), 2, RoundingMode.HALF_UP);
        }

        // 2. Structurally archive current configuration baseline to policy_history
        PolicyHistory history = PolicyHistory.builder()
                .policy(policy)
                .policyNumber(policy.getPolicyNumber())
                .productType(policy.getProductType())
                .basePremium(policy.getBasePremium())
                .activeReserve(policy.getActiveReserve())
                .expiryDate(policy.getExpiryDate())
                .status(policy.getStatus())
                .modifiedBy(modifier.getEmail())
                .build();
        policyHistoryRepository.save(history);

        // 3. Apply structural adjustments to main active policy
        policy.setProductType(request.getProductType());
        policy.setBasePremium(request.getBasePremium());
        policy.setExpiryDate(request.getExpiryDate());
        policy.setStatus("Under Review"); // Term changes undergo approval check

        policyRepository.save(policy);

        auditLogService.log("Policy Endorsed", policy.getPolicyNumber(), 
                "Product: " + request.getProductType() + ", Premium adjusted: $" + oldPremium + " -> $" + request.getBasePremium() + ", Pro-rata adjustment: $" + proRataAdjustment);

        return EndorsementResponse.builder()
                .policyNumber(policy.getPolicyNumber())
                .oldPremium(oldPremium)
                .newPremium(newPremium)
                .proRataAdjustment(proRataAdjustment)
                .daysRemaining(daysRemaining)
                .message("Policy snapshot archived. Pro-rata premium adjustment calculated successfully.")
                .build();
    }

    @Override
    public List<PolicyHistoryDto> getPolicyHistory(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return policyHistoryRepository.findByPolicyIdOrderByModifiedAtDesc(id).stream()
                .map(h -> PolicyHistoryDto.builder()
                        .id(h.getId())
                        .policyNumber(h.getPolicyNumber())
                        .productType(h.getProductType())
                        .basePremium(h.getBasePremium())
                        .activeReserve(h.getActiveReserve())
                        .expiryDate(h.getExpiryDate().toString())
                        .status(h.getStatus())
                        .modifiedAt(h.getModifiedAt() != null ? h.getModifiedAt().format(formatter) : "")
                        .modifiedBy(h.getModifiedBy())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Policy createPolicy(PolicyCreateRequest request) {
        // Enforce permissions: Creating policies is restricted to Underwriter, Agent, or Admin roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated");
        }

        String email = auth.getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (creator.getRole() == Role.POLICYHOLDER) {
            throw new AccessDeniedException("Access denied: Policyholders cannot create policies");
        }

        // Find referencing policyholder by email
        String searchEmail = request.getPolicyholderEmail().trim().toLowerCase();
        User policyholder = userRepository.findByEmail(searchEmail)
                .orElseThrow(() -> new IllegalArgumentException("Policyholder user not found with email: " + request.getPolicyholderEmail()));

        if (policyholder.getRole() != Role.POLICYHOLDER) {
            throw new IllegalArgumentException("Target user email does not belong to a policyholder role");
        }

        // Check if policy number is unique
        if (policyRepository.existsByPolicyNumber(request.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number is already in use");
        }

        Policy policy = Policy.builder()
                .policyNumber(request.getPolicyNumber())
                .policyholder(policyholder)
                .productType(request.getProductType())
                .basePremium(request.getBasePremium())
                .activeReserve(BigDecimal.ZERO)
                .expiryDate(request.getExpiryDate())
                .status("Active")
                .build();

        Policy saved = policyRepository.save(policy);

        // Record audit log
        auditLogService.log("Policy Created", saved.getPolicyNumber(),
                "Product: " + saved.getProductType() + ", Premium: $" + saved.getBasePremium() + ", Policyholder: " + policyholder.getEmail());

        return saved;
    }

    @Override
    @Transactional
    public Policy renewPolicy(Long id) {
        // Enforce permissions: Renewing policies is restricted to Underwriter, Agent, or Admin roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated");
        }

        String email = auth.getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (creator.getRole() == Role.POLICYHOLDER) {
            throw new AccessDeniedException("Access denied: Policyholders cannot renew policies");
        }

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));

        // Extend expiry date by 1 year (365 days)
        LocalDate newExpiry = policy.getExpiryDate().plusYears(1);
        policy.setExpiryDate(newExpiry);
        policy.setStatus("Active");

        Policy saved = policyRepository.save(policy);

        // Record audit log
        auditLogService.log("Policy Renewed", saved.getPolicyNumber(),
                "Term extended by 1 year to: " + saved.getExpiryDate());

        return saved;
    }
}
