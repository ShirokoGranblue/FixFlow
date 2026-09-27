package com.lz.fixflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.fixflow.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
