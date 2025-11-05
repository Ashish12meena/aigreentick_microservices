package com.aigreentick.services.notification.service.ratelimit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.EmailProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Production-ready Redis-based rate limiter using sliding window algorithm
 * Works across multiple service instances without any Bucket4j dependency
 */
@Slf4j
@Service
public class RedisRateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailProperties emailProperties;
    
    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:email:";
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);
    
    // Lua script for atomic rate limiting check
    private static final String RATE_LIMIT_LUA_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local current_time = tonumber(ARGV[3]) " +
        "local window_start = current_time - window " +
        
        // Remove old entries outside the window
        "redis.call('ZREMRANGEBYSCORE', key, 0, window_start) " +
        
        // Count current requests in window
        "local current_count = redis.call('ZCARD', key) " +
        
        // Check if limit exceeded
        "if current_count < limit then " +
        "  redis.call('ZADD', key, current_time, current_time) " +
        "  redis.call('EXPIRE', key, window) " +
        "  return {1, limit - current_count - 1} " +
        "else " +
        "  return {0, 0} " +
        "end";

    private final DefaultRedisScript<List> rateLimitScript;

    public RedisRateLimiterService(
            RedisTemplate<String, String> redisTemplate,
            EmailProperties emailProperties) {
        
        this.redisTemplate = redisTemplate;
        this.emailProperties = emailProperties;
        
        // Prepare Lua script
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        this.rateLimitScript.setResultType(List.class);
        
        log.info("Redis Rate Limiter Service initialized");
    }

    /**
     * Check if email can be sent based on distributed rate limits
     */
    public boolean allowEmail(String userId, String ipAddress) {
        if (!emailProperties.getRateLimit().isEnabled()) {
            return true;
        }

        try {
            // Check global rate limit
            if (!checkLimitWithLua("global", 
                    emailProperties.getRateLimit().getGlobal().getRequestsPerMinute())) {
                log.warn("Global rate limit exceeded");
                return false;
            }

            // Check per-user rate limit
            if (userId != null && !checkLimitWithLua("user:" + userId, 
                    emailProperties.getRateLimit().getPerUser().getRequestsPerMinute())) {
                log.warn("User rate limit exceeded for userId: {}", userId);
                return false;
            }

            // Check per-IP rate limit
            if (ipAddress != null && !checkLimitWithLua("ip:" + ipAddress, 
                    emailProperties.getRateLimit().getPerIp().getRequestsPerMinute())) {
                log.warn("IP rate limit exceeded for IP: {}", ipAddress);
                return false;
            }

            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limit", e);
            // Fail open - allow request if Redis is down
            return true;
        }
    }

    /**
     * Check rate limit using Lua script for atomic operation
     */
    private boolean checkLimitWithLua(String key, int maxRequests) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowMs = WINDOW_DURATION.toMillis();

        try {
            List<String> keys = new ArrayList<>();
            keys.add(redisKey);

            Object[] args = new Object[] {
                String.valueOf(maxRequests),
                String.valueOf(windowMs),
                String.valueOf(currentTime)
            };

            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) redisTemplate.execute(
                    rateLimitScript, 
                    keys, 
                    args);

            if (result != null && !result.isEmpty()) {
                return result.get(0) == 1L;
            }

            return false;

        } catch (Exception e) {
            log.error("Error executing rate limit Lua script for key: {}", redisKey, e);
            // Fail open
            return true;
        }
    }

    /**
     * Simple counter-based check (fallback if Lua script fails)
     */
    private boolean checkLimitSimple(String key, int maxRequests) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        
        try {
            String currentCountStr = redisTemplate.opsForValue().get(redisKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            if (currentCount >= maxRequests) {
                return false;
            }
            
            Long newCount = redisTemplate.opsForValue().increment(redisKey);
            
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(redisKey, WINDOW_DURATION);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error in simple rate limit check for key: {}", redisKey, e);
            return true;
        }
    }

    /**
     * Get remaining tokens for user
     */
    public long getRemainingTokensForUser(String userId) {
        return getRemainingTokens("user:" + userId, 
                emailProperties.getRateLimit().getPerUser().getRequestsPerMinute());
    }

    /**
     * Get remaining tokens for IP
     */
    public long getRemainingTokensForIp(String ipAddress) {
        return getRemainingTokens("ip:" + ipAddress, 
                emailProperties.getRateLimit().getPerIp().getRequestsPerMinute());
    }

    /**
     * Get remaining tokens for a specific key
     */
    private long getRemainingTokens(String key, int maxRequests) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - WINDOW_DURATION.toMillis();
        
        try {
            // Remove old entries
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            
            // Count current entries
            Long currentCount = redisTemplate.opsForZSet().zCard(redisKey);
            
            if (currentCount == null) {
                return maxRequests;
            }
            
            return Math.max(0, maxRequests - currentCount);
            
        } catch (Exception e) {
            log.error("Error getting remaining tokens for key: {}", redisKey, e);
            return maxRequests;
        }
    }

    /**
     * Reset rate limits for user
     */
    public void resetUserLimit(String userId) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + "user:" + userId;
        redisTemplate.delete(redisKey);
        log.info("Reset rate limit for userId: {}", userId);
    }

    /**
     * Reset rate limits for IP
     */
    public void resetIpLimit(String ipAddress) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + "ip:" + ipAddress;
        redisTemplate.delete(redisKey);
        log.info("Reset rate limit for IP: {}", ipAddress);
    }

    /**
     * Get current count for debugging
     */
    public int getCurrentCount(String key) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - WINDOW_DURATION.toMillis();
        
        try {
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            Long count = redisTemplate.opsForZSet().zCard(redisKey);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("Error getting current count for key: {}", redisKey, e);
            return 0;
        }
    }

    /**
     * Clean up old rate limit entries (optional maintenance)
     */
    public void cleanup() {
        try {
            Set<String> keys = redisTemplate.keys(RATE_LIMIT_KEY_PREFIX + "*");
            if (keys != null) {
                long currentTime = System.currentTimeMillis();
                long windowStart = currentTime - WINDOW_DURATION.toMillis();
                
                for (String key : keys) {
                    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
                }
                
                log.debug("Cleaned up {} rate limit keys", keys.size());
            }
        } catch (Exception e) {
            log.error("Error during rate limit cleanup", e);
        }
    }
}