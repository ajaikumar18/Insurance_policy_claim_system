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
public class PolicyDto {
    private Long id;
    private String policyNumber;
    private Long policyholderId;
    private String policyholderName;
    private String productType;
    private BigDecimal basePremium;
    private BigDecimal activeReserve;
    private String expiryDate;
    private String status;
}
