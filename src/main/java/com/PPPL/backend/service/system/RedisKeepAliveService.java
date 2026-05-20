package com.PPPL.backend.service.system;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisKeepAliveService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisKeepAliveService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void ping() {
        redisTemplate.opsForValue().set(
            "system:keepalive",
            System.currentTimeMillis()
        );
    }
}