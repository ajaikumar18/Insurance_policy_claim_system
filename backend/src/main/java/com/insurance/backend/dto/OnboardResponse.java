package com.insurance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String amlStatus;
    private String kycStatus;
    private String message;
}
