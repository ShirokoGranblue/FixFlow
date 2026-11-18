package com.lz.fixflow;

import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.cache.DashboardCache;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.service.TicketService;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest(properties = {
        "spring.data.redis.host=127.0.0.1", "spring.data.redis.port=${FIXFLOW_TEST_REDIS_PORT}"
})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "FIXFLOW_TEST_REDIS_PORT", matches = "[0-9]+")
class DashboardCacheTransactionIntegrationTest {
    @Autowired TicketService tickets;
    @Autowired JdbcTemplate jdbc;
    @Autowired StringRedisTemplate redis;
    @Autowired TransactionTemplate transactions;

    @ParameterizedTest(name = "createTicket: rollback={0}")
    @ValueSource(booleans = {false, true})
    void invalidatesOnlyAfterCommit(boolean rollback) {
        assertEquals("fixflow_test", jdbc.queryForObject("SELECT DATABASE()", String.class));
        String marker = UUID.randomUUID().toString();
        assertEquals(Boolean.TRUE, redis.opsForValue().setIfAbsent(DashboardCache.KEY, marker));
        try (var login = mockStatic(StpUtil.class)) {
            login.when(StpUtil::isLogin).thenReturn(true);
            login.when(StpUtil::getLoginIdAsString).thenReturn(marker);
            jdbc.update("INSERT INTO `user` (id, username, password, email) VALUES (?, ?, '', ?)",
                    marker, marker, marker + "@example.invalid");
            jdbc.update("INSERT INTO category (name) VALUES (?)", marker);
            TicketCreateDTO dto = new TicketCreateDTO();
            dto.setCategoryId(jdbc.queryForObject("SELECT id FROM category WHERE name = ?", Long.class, marker));
            dto.setTitle(marker);
            dto.setDescription("Dashboard cache transaction test");
            dto.setPriority("MEDIUM");
            String id = transactions.execute(status -> {
                String ticketId = tickets.createTicket(dto).getId();
                assertEquals(1, ticketCount(ticketId));
                assertEquals(marker, redis.opsForValue().get(DashboardCache.KEY));
                if (rollback) status.setRollbackOnly();
                return ticketId;
            });
            assertEquals(rollback ? 0 : 1, ticketCount(id));
            assertEquals(rollback ? marker : null, redis.opsForValue().get(DashboardCache.KEY));
        } finally {
            redis.delete(DashboardCache.KEY);
            jdbc.update("DELETE FROM ticket_history WHERE operator_id = ?", marker);
            jdbc.update("DELETE FROM ticket WHERE creator_id = ?", marker);
            jdbc.update("DELETE FROM audit_log WHERE user_id = ?", marker);
            jdbc.update("DELETE FROM category WHERE name = ?", marker);
            jdbc.update("DELETE FROM `user` WHERE id = ?", marker);
        }
    }

    private int ticketCount(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM ticket WHERE id = ?", Integer.class, id);
    }
}
