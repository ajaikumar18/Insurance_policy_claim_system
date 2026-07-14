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
public class ClaimDto {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String policyholderName;
    private String incidentType;
    private String incidentDate;
    private String incidentLocation;
    private BigDecimal grossReserve;
    private String lossDescription;
    private String adjuster;
    private String status;
}
