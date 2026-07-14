package com.insurance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EndorsementRequest {

    @NotBlank(message = "Product type is required")
    private String productType;

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "0.0", message = "Premium cannot be negative")
    private BigDecimal basePremium;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
