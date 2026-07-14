package com.insurance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverrideResponse {
    private Long id;
    private String policyNumber;
    private String overrideType;
    private String deltaValue;
    private String requestedBy;
    private String status;
    private String supervisorSignoffBy;
    private String createdAt;
}
