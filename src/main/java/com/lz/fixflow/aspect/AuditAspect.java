package com.lz.fixflow.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private AuditLogMapper auditLogMapper;

    @Around("@annotation(audit)")
    public Object Around(
            ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {

        long start = System.currentTimeMillis();

        String userid = null;
        if(StpUtil.isLogin()){
            userid = StpUtil.getLoginIdAsString();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        try{

            Object result = joinPoint.proceed();
            saveLog(
                    userid,
                    audit.value(),
                    true,
                    null,
                    System.currentTimeMillis()-start,
                    getIp(request),
                    joinPoint
            );

            return result;

        }catch(Throwable e){

            saveLog(
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
    }

    private void saveLog(String userid,
                         String action,
                         boolean success,
                         String errorMessage,
                         long durationMs,
                         String ip,
                         ProceedingJoinPoint joinPoint) {
        try {
            AuditLog log = new AuditLog();

            log.setUserid(userid);
            log.setAction(action);
            log.setSuccess(success);
            log.setErrorMessage(errorMessage);
            log.setDurationMs(durationMs);
            log.setCreatedAt(LocalDateTime.now());
            log.setIp(ip);

            log.setMethod(joinPoint.getSignature().toShortString());

            auditLogMapper.insert(log);

        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private String getIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
