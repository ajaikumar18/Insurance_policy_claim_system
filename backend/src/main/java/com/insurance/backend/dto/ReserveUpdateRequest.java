package com.insurance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReserveUpdateRequest {

    @NotNull(message = "Gross reserve total is required")
    @DecimalMin(value = "0.0", message = "Gross reserve cannot be negative")
    private BigDecimal grossReserve;
}
