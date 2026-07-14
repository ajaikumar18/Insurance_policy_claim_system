package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.UnderwriteOverride;
import com.insurance.backend.repository.UnderwriteOverrideRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnderwriteServiceImpl implements UnderwriteService {

    private final UnderwriteOverrideRepository overrideRepository;
    private final AuditLogService auditLogService;

    public UnderwriteServiceImpl(UnderwriteOverrideRepository overrideRepository, AuditLogService auditLogService) {
        this.overrideRepository = overrideRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public QuoteResponse calculatePremiumQuote(QuoteRequest request) {
        // Compute average score exactly matching frontend Figma behavior
        double compositeScore = (request.getRegionScore() +
                request.getAssetAgeScore() +
                request.getPriorClaimsScore() +
                request.getBusinessTypeScore()) / 4.0;

        int compositeRounded = (int) Math.round(compositeScore);

        String riskLevel;
        if (compositeRounded >= 70) {
            riskLevel = "HIGH RISK";
        } else if (compositeRounded >= 45) {
            riskLevel = "MEDIUM RISK";
        } else {
            riskLevel = "LOW RISK";
        }

        // Formulaic calculation of the base premium
        // E.g., base premium = $500.00 base + ($15.00 * compositeScore)
        BigDecimal premiumQuote = BigDecimal.valueOf(500.00 + (15.00 * compositeScore))
                .setScale(2, RoundingMode.HALF_UP);

        return QuoteResponse.builder()
                .compositeRiskScore(compositeRounded)
                .riskLevel(riskLevel)
                .premiumQuote(premiumQuote)
                .build();
    }

    @Override
    @Transactional
    public OverrideResponse submitOverride(OverrideRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requestedBy = auth != null ? auth.getName() : "anonymous";

        UnderwriteOverride override = UnderwriteOverride.builder()
                .policyNumber(request.getPolicyNumber())
                .overrideType(request.getOverrideType())
                .deltaValue(request.getDeltaValue())
                .requestedBy(requestedBy)
                .status("PENDING")
                .build();

        UnderwriteOverride saved = overrideRepository.save(override);
        
        auditLogService.log("Override Requested", override.getPolicyNumber(), 
                "Override type '" + override.getOverrideType() + "' with delta '" + override.getDeltaValue() + "' requested by: " + requestedBy);

        return mapToDto(saved);
    }

    @Override
    public List<OverrideResponse> getAllOverrides() {
        return overrideRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OverrideResponse approveOverride(Long id) {
        UnderwriteOverride override = overrideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Override request not found with ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String supervisor = auth != null ? auth.getName() : "supervisor";

        override.setStatus("APPROVED");
        override.setSupervisorSignoffBy(supervisor);

        UnderwriteOverride saved = overrideRepository.save(override);
        
        auditLogService.log("Override Approved", override.getPolicyNumber(), 
                "Override type '" + override.getOverrideType() + "' with delta '" + override.getDeltaValue() + "' was approved by supervisor: " + supervisor);

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OverrideResponse rejectOverride(Long id) {
        UnderwriteOverride override = overrideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Override request not found with ID: " + id));

        override.setStatus("REJECTED");

        UnderwriteOverride saved = overrideRepository.save(override);
        
        auditLogService.log("Override Rejected", override.getPolicyNumber(), 
                "Override type '" + override.getOverrideType() + "' with delta '" + override.getDeltaValue() + "' was rejected");

        return mapToDto(saved);
    }

    private OverrideResponse mapToDto(UnderwriteOverride o) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = o.getCreatedAt() != null ? o.getCreatedAt().format(formatter) : "";

        return OverrideResponse.builder()
                .id(o.getId())
                .policyNumber(o.getPolicyNumber())
                .overrideType(o.getOverrideType())
                .deltaValue(o.getDeltaValue())
                .requestedBy(o.getRequestedBy())
                .status(o.getStatus())
                .supervisorSignoffBy(o.getSupervisorSignoffBy())
                .createdAt(formattedDate)
                .build();
    }
}
