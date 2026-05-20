package com.PPPL.backend.controller.system;

import com.PPPL.backend.service.system.RedisKeepAliveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system")
public class RedisKeepAliveController {

    private final RedisKeepAliveService redisKeepAliveService;

    public RedisKeepAliveController(RedisKeepAliveService redisKeepAliveService) {
        this.redisKeepAliveService = redisKeepAliveService;
    }

    @Value("${KEEPALIVE_SECRET}")
    private String keepAliveSecret;

    @GetMapping("/keepalive/redis")
    public ResponseEntity<?> keepAliveRedis(
            @RequestHeader(value = "X-API-KEY", required = false) String key
    ) {
        if (key == null || !keepAliveSecret.equals(key)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        try {
            redisKeepAliveService.ping();
            return ResponseEntity.ok("Redis OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Redis ERROR: " + e.getMessage());
        }
    }
}