package com.lz.fixflow.service;

import com.lz.fixflow.cache.DashboardCache;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.service.impl.DashBoardServiceImpl;
import com.lz.fixflow.vo.DashBoardVO;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DashBoardCacheTest {

    @Test
    void shouldRebuildCacheOnlyOnce() throws Exception {

        TicketMapper tickets = mock(TicketMapper.class);
        CategoryMapper categories = mock(CategoryMapper.class);

        StringRedisTemplate redis =
                mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops =
                mock(ValueOperations.class);

        when(redis.opsForValue()).thenReturn(ops);

        // 模拟 Redis 中存储的 JSON
        AtomicReference<String> cache =
                new AtomicReference<>();

        int threads = 20;

        CyclicBarrier firstReadBarrier =
                new CyclicBarrier(threads);

        AtomicInteger reads = new AtomicInteger();

        when(ops.get(DashboardCache.KEY))
                .thenAnswer(invocation -> {

                    // 强制所有线程第一次读取均未命中
                    if (reads.getAndIncrement() < threads) {
                        firstReadBarrier.await(
                                5, TimeUnit.SECONDS
                        );
                        return null;
                    }

                    return cache.get();
                });

        doAnswer(invocation -> {
            cache.set(invocation.getArgument(1));
            return null;
        }).when(ops).set(
                eq(DashboardCache.KEY),
                anyString(),
                any(Duration.class)
        );

        when(tickets.selectCount(any()))
                .thenReturn(10L);

        when(categories.countCategory())
                .thenReturn(List.of());

        DashBoardServiceImpl service =
                new DashBoardServiceImpl(
                        tickets,
                        categories,
                        JsonMapper.builder().build(),
                        redis
                );

        ExecutorService pool =
                Executors.newFixedThreadPool(threads);

        List<Future<DashBoardVO>> futures =
                new ArrayList<>();

        CountDownLatch start = new CountDownLatch(1);

        try {

            for (int i = 0; i < threads; i++) {

                futures.add(pool.submit(() -> {
                    start.await();
                    return service.getDashBoard();
                }));
            }

            // 同时释放所有线程
            start.countDown();

            for (Future<DashBoardVO> future : futures) {

                DashBoardVO dashboard =
                        future.get(10, TimeUnit.SECONDS);

                assertEquals(10L, dashboard.getTotalCount());
            }

            // Dashboard 只执行一次数据库统计
            verify(tickets, times(6))
                    .selectCount(any());

            verify(categories, times(1))
                    .countCategory();

            // Redis 只写入一次
            verify(ops, times(1)).set(
                    eq(DashboardCache.KEY),
                    anyString(),
                    any(Duration.class)
            );

        } finally {
            pool.shutdownNow();
        }
    }
}