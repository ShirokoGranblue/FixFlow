package com.lz.fixflow.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.PageQueryDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.service.AuditLogService;
import com.lz.fixflow.vo.TicketVO;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@SaCheckRole(value = "ADMIN")
@RequiredArgsConstructor
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public Result<PageResult<AuditLog>> searchTickets(@Valid @RequestBody PageQueryDTO dto) {
        return Result.success(
                auditLogService.getAuditLog(dto.getCurrent(),dto.getPageSize())
            );
    }
}
