package com.insurance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCreateRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotBlank(message = "Policyholder email is required")
    @Email(message = "Invalid email format")
    private String policyholderEmail;

    @NotBlank(message = "Product type is required")
    private String productType;

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "0.00", message = "Base premium must be positive")
    private BigDecimal basePremium;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
