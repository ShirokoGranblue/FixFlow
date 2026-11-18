package com.lz.fixflow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class RedisConnectTest {

    @Autowired private StringRedisTemplate template;

    @Test
    void shouldReadAndWriteRedis(){
        String key = "flxflow:test:"+UUID.randomUUID();
        try{
            template.opsForValue().set(key,"PONG",Duration.ofSeconds(30));
            assertEquals("PONG",template.opsForValue().get(key));
        }finally{
            template.delete(key);
        }
    }
}
