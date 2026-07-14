package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.Policy;
import java.util.List;

public interface PolicyService {
    List<Policy> getPoliciesByScope();
    Policy getPolicyById(Long id);
    EndorsementResponse endorsePolicy(Long id, EndorsementRequest request);
    List<PolicyHistoryDto> getPolicyHistory(Long id);
}
