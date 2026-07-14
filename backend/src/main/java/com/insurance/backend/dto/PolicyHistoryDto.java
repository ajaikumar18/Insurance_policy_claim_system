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
public class PolicyHistoryDto {
    private Long id;
    private String policyNumber;
    private String productType;
    private BigDecimal basePremium;
    private BigDecimal activeReserve;
    private String expiryDate;
    private String status;
    private String modifiedAt;
    private String modifiedBy;
}
