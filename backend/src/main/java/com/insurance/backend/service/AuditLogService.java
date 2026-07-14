package com.insurance.backend.service;

import com.insurance.backend.entity.AuditLog;
import java.util.List;

public interface AuditLogService {
    void log(String action, String target, String detail);
    List<AuditLog> getAllLogs();
}
