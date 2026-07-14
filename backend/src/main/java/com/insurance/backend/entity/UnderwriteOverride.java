package com.insurance.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "underwrite_overrides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderwriteOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, length = 30)
    private String policyNumber;

    @Column(name = "override_type", nullable = false)
    private String overrideType;

    @Column(name = "delta_value", nullable = false)
    private String deltaValue;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "supervisor_signoff_by")
    private String supervisorSignoffBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
