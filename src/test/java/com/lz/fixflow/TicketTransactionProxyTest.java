package com.lz.fixflow;

import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事务机制学习测试：检查 Spring 代理和事务元数据。
 * 不会直接修改业务数据；@SpringBootTest 启动时仍需有效的测试数据库。
 */
@SpringBootTest
@ActiveProfiles("test")
class TicketTransactionProxyTest {

    @Autowired
    private TicketService ticketService;

    @Test
    @DisplayName("TicketService 是 Spring AOP 代理，且具有事务拦截器")
    void serviceHasTransactionProxy() {
        assertTrue(AopUtils.isAopProxy(ticketService),
                "期望注入的是 Spring AOP 代理，而不是直接 new 出来的 Service");

        Advised adv = (Advised) ticketService;
        Advised advised = assertInstanceOf(Advised.class, ticketService);
        boolean hasTransactionAdvice = Arrays.stream(advised.getAdvisors())
                .anyMatch(advisor -> advisor.getAdvice() instanceof TransactionInterceptor);
        assertTrue(hasTransactionAdvice,"期望代理的拦截器链中存在 TransactionInterceptor");
    }

    @Test
    @DisplayName("handleTicket 设置了包含受检异常的回滚规则")
    void handleTicketHasRollbackRule() throws NoSuchMethodException {
        Method method = TicketServiceImpl.class.getMethod("handleTicket", String.class);
        var source = new AnnotationTransactionAttributeSource();
        var attribute = source.getTransactionAttribute(method, TicketServiceImpl.class);

        assertNotNull(attribute, "handleTicket 应当具有 @Transactional 元数据");
        assertTrue(attribute.rollbackOn(new Exception("模拟受检异常")),
                "rollbackFor = Exception.class 应当匹配受检异常");
    }
}
