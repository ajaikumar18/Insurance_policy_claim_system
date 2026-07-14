package com.insurance.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FnolRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @NotBlank(message = "Incident type is required")
    private String incidentType;

    @NotBlank(message = "Incident location is required")
    private String incidentLocation;

    @NotBlank(message = "Loss description is required")
    private String lossDescription;
}
