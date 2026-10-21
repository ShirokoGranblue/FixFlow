package com.lz.fixflow.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(audit)")
    public Object Around(
            ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {

        long start = System.currentTimeMillis();

        String userid = null;
        if(StpUtil.isLogin()){
            userid = StpUtil.getLoginIdAsString();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes==null) {
            throw new IllegalStateException("HTTP 请求上下文不存在");
        }
        HttpServletRequest request = attributes.getRequest();

        Object result;
        try{
            result = joinPoint.proceed();

        }catch(Throwable e){
            auditLogService.saveLog(
                userid,
                audit.value(),
                false,
                e.getMessage(),
                System.currentTimeMillis()-start,
                getIp(request),
                joinPoint
            );
            throw e;
        }
        auditLogService.saveLog(
            userid,
            audit.value(),
            true,
            null,
            System.currentTimeMillis()-start,
            getIp(request),
            joinPoint
        );

        return result;
    }

    public static String getIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
