package com.insurance.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuoteRequest {

    @NotNull(message = "Regional risk score is required")
    @Min(0) @Max(100)
    private Integer regionScore;

    @NotNull(message = "Asset age factor score is required")
    @Min(0) @Max(100)
    private Integer assetAgeScore;

    @NotNull(message = "Prior claims frequency score is required")
    @Min(0) @Max(100)
    private Integer priorClaimsScore;

    @NotNull(message = "Business classification score is required")
    @Min(0) @Max(100)
    private Integer businessTypeScore;
}
