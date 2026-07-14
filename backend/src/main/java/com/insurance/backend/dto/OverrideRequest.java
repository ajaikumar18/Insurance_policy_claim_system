package com.insurance.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OverrideRequest {

    @NotBlank(message = "Policy number reference is required")
    private String policyNumber;

    @NotBlank(message = "Override type is required")
    private String overrideType;

    @NotBlank(message = "Delta value indicator is required")
    private String deltaValue;
}
