package com.lz.fixflow.event;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.lz.fixflow.cache.DashboardCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j 
public class DashboardCacheInvalidationListener {
    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDashboardChanged(DashboardChangedEvent event){
        try{
            redisTemplate.delete(DashboardCache.KEY);
            log.debug("Dashboard cache invalidated");
        }catch(DataAccessException e){
            log.warn("Failed to invalidate Dashboard cache");
        }
    }
}
