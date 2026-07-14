package com.insurance.backend.service;

import com.insurance.backend.dto.*;

import java.util.List;

public interface ClaimService {
    ClaimDto registerFnol(FnolRequest request);
    List<ClaimDto> getClaimsByScope();
    ClaimDto updateReserve(Long id, ReserveUpdateRequest request);
}
