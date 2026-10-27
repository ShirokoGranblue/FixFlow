package com.lz.fixflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.AuditLogMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<AuditLog> getAuditLog(long current, long pageSize) {

        long size = Math.min(pageSize,100);

        Page<AuditLog> page = new Page<>(current,size);
        Page<AuditLog> result = auditLogMapper.selectPage(page,
                new LambdaQueryWrapper<AuditLog>()
                        .orderByDesc(AuditLog::getCreatedAt)
        );

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getPages(),
                result.getSize()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String userid,
                         String action,
                         boolean success,
                         String errorMessage,
                         long durationMs,
                         String ip,
                         ProceedingJoinPoint joinPoint){
        try {
            AuditLog auditLog = new AuditLog();
            User user = userMapper.selectById(userid);

            auditLog.setUserId(userid);
            auditLog.setUsername(user.getUsername());
            auditLog.setAction(action);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setDurationMs(durationMs);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLog.setIp(ip);

            auditLog.setMethod(joinPoint.getSignature().toShortString());

            auditLogMapper.insert(auditLog);

        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
