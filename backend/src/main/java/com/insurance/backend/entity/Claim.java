package com.insurance.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", unique = true, nullable = false, length = 30)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_type", length = 100)
    private String incidentType;

    @Column(name = "incident_location", length = 255)
    private String incidentLocation;

    @Builder.Default
    @Column(name = "gross_reserve", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossReserve = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String lossDescription;

    @Column(nullable = false)
    private String status; // e.g., PENDING_FNOL, UNDER_INVESTIGATION, APPROVED, FRAUD_ALERT
}
