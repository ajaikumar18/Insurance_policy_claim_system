package com.insurance.backend.controller;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.Policy;
import com.insurance.backend.service.PolicyService;
import com.insurance.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PolicyController {

    private final UserService userService;
    private final PolicyService policyService;

    public PolicyController(UserService userService, PolicyService policyService) {
        this.userService = userService;
        this.policyService = policyService;
    }

    @PostMapping("/policy/onboard")
    public ResponseEntity<OnboardResponse> onboardPolicyholder(@Valid @RequestBody OnboardRequest request) {
        OnboardResponse response = userService.onboardPolicyholder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/policy/all")
    public ResponseEntity<List<PolicyDto>> getAllPoliciesByScope() {
        List<PolicyDto> policies = policyService.getPoliciesByScope().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/policies")
    public ResponseEntity<List<PolicyDto>> getPolicies() {
        List<PolicyDto> policies = policyService.getPoliciesByScope().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/policies/{id}/endorse")
    public ResponseEntity<EndorsementResponse> endorsePolicy(@PathVariable Long id, @Valid @RequestBody EndorsementRequest request) {
        EndorsementResponse response = policyService.endorsePolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policies/{id}/history")
    public ResponseEntity<List<PolicyHistoryDto>> getPolicyHistory(@PathVariable Long id) {
        List<PolicyHistoryDto> history = policyService.getPolicyHistory(id);
        return ResponseEntity.ok(history);
    }

    private PolicyDto mapToDto(Policy policy) {
        return PolicyDto.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyholderId(policy.getPolicyholder().getId())
                .policyholderName(policy.getPolicyholder().getLegalName() != null ? 
                        policy.getPolicyholder().getLegalName() : policy.getPolicyholder().getUsername())
                .productType(policy.getProductType())
                .basePremium(policy.getBasePremium())
                .activeReserve(policy.getActiveReserve())
                .expiryDate(policy.getExpiryDate().toString())
                .status(policy.getStatus())
                .build();
    }
}
