package com.lz.fixflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.mapper.AuditLogMapper;
import com.lz.fixflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

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

}
