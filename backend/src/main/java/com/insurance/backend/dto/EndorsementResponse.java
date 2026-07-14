package com.insurance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndorsementResponse {
    private String policyNumber;
    private BigDecimal oldPremium;
    private BigDecimal newPremium;
    private BigDecimal proRataAdjustment;
    private long daysRemaining;
    private String message;
}
