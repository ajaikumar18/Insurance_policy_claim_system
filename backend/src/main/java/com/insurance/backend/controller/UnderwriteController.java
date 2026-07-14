package com.insurance.backend.controller;

import com.insurance.backend.dto.*;
import com.insurance.backend.service.UnderwriteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/underwrite")
public class UnderwriteController {

    private final UnderwriteService underwriteService;

    public UnderwriteController(UnderwriteService underwriteService) {
        this.underwriteService = underwriteService;
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> calculateQuote(@Valid @RequestBody QuoteRequest request) {
        QuoteResponse response = underwriteService.calculatePremiumQuote(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/override")
    public ResponseEntity<OverrideResponse> submitOverride(@Valid @RequestBody OverrideRequest request) {
        OverrideResponse response = underwriteService.submitOverride(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overrides")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    public ResponseEntity<List<OverrideResponse>> getAllOverrides() {
        List<OverrideResponse> response = underwriteService.getAllOverrides();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/override/{id}/approve")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    public ResponseEntity<OverrideResponse> approveOverride(@PathVariable Long id) {
        OverrideResponse response = underwriteService.approveOverride(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/override/{id}/reject")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    public ResponseEntity<OverrideResponse> rejectOverride(@PathVariable Long id) {
        OverrideResponse response = underwriteService.rejectOverride(id);
        return ResponseEntity.ok(response);
    }
}
