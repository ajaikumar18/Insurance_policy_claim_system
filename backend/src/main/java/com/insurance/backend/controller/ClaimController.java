package com.insurance.backend.controller;

import com.insurance.backend.dto.*;
import com.insurance.backend.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/fnol")
    public ResponseEntity<ClaimDto> registerFnol(@Valid @RequestBody FnolRequest request) {
        ClaimDto response = claimService.registerFnol(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClaimDto>> getClaimsByScope() {
        List<ClaimDto> response = claimService.getClaimsByScope();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reserve/{id}")
    public ResponseEntity<ClaimDto> updateReserve(@PathVariable Long id, @Valid @RequestBody ReserveUpdateRequest request) {
        ClaimDto response = claimService.updateReserve(id, request);
        return ResponseEntity.ok(response);
    }
}
