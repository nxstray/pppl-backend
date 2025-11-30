package com.PPPL.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterConfig {
    
    // Store buckets per admin ID
    private final Map<Integer, Bucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Get or create bucket for admin
     * 15 requests per 2 minutes
     */
    public Bucket resolveBucket(Integer adminId) {
        return buckets.computeIfAbsent(adminId, k -> createNewBucket());
    }
    
    /**
     * Create new bucket with limit: 15 requests per 2 minutes
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(15)
            .refillIntervally(15, Duration.ofMinutes(2))
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * Clear bucket for admin (for testing or reset)
     */
    public void clearBucket(Integer adminId) {
        buckets.remove(adminId);
    }
    
    /**
     * Get remaining tokens for admin
     */
    public long getRemainingTokens(Integer adminId) {
        Bucket bucket = resolveBucket(adminId);
        return bucket.getAvailableTokens();
    }
}