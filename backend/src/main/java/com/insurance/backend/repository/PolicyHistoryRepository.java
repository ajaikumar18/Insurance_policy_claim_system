package com.insurance.backend.repository;

import com.insurance.backend.entity.PolicyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyHistoryRepository extends JpaRepository<PolicyHistory, Long> {
    List<PolicyHistory> findByPolicyIdOrderByModifiedAtDesc(Long policyId);
    List<PolicyHistory> findByPolicyNumberOrderByModifiedAtDesc(String policyNumber);
}
