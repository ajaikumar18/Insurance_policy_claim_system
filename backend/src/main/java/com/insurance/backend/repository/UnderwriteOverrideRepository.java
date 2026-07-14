package com.insurance.backend.repository;

import com.insurance.backend.entity.UnderwriteOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnderwriteOverrideRepository extends JpaRepository<UnderwriteOverride, Long> {
    List<UnderwriteOverride> findByPolicyNumber(String policyNumber);
}
