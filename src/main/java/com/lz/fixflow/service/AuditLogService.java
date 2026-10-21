package com.lz.fixflow.service;

import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.entity.AuditLog;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

@Service
public interface AuditLogService {

    PageResult<AuditLog> getAuditLog(long current,long pageSize);

    void saveLog(String userid,
                         String action,
                         boolean success,
                         String errorMessage,
                         long durationMs,
                         String ip,
                         ProceedingJoinPoint joinPoint);
}
