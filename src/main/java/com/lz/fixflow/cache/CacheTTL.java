package com.lz.fixflow.cache;

import java.time.Duration;

import io.netty.util.internal.ThreadLocalRandom;

public final class CacheTTL {
    private CacheTTL(){
    }

    public static Duration randomSeconds(long minSeconds,long maxSeconds){
        long seconds = ThreadLocalRandom.current()
                                    .nextLong(minSeconds,maxSeconds+1);
        return Duration.ofSeconds(seconds);
    }
}