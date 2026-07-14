package com.insurance.backend.service;

import com.insurance.backend.dto.*;

import java.util.List;

public interface UnderwriteService {
    QuoteResponse calculatePremiumQuote(QuoteRequest request);
    OverrideResponse submitOverride(OverrideRequest request);
    List<OverrideResponse> getAllOverrides();
    OverrideResponse approveOverride(Long id);
    OverrideResponse rejectOverride(Long id);
}
