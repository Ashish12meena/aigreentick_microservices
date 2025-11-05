package com.aigreentick.services.notification.service.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.EmailProperties;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRateLimiterService {

    private final EmailProperties emailProperties;
    // private final RedisTemplate<String, String> redisTemplate;
    
    private final Map<String, LocalBucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, LocalBucket> ipBuckets = new ConcurrentHashMap<>();
    private LocalBucket globalBucket;

    /**
     * Check if email can be sent based on rate limits
     */
    public boolean allowEmail(String userId, String ipAddress) {
        if (!emailProperties.getRateLimit().isEnabled()) {
            return true;
        }

        // Check global rate limit
        if (!checkGlobalLimit()) {
            log.warn("Global rate limit exceeded");
            return false;
        }

        // Check per-user rate limit
        if (userId != null && !checkUserLimit(userId)) {
            log.warn("User rate limit exceeded for userId: {}", userId);
            return false;
        }

        // Check per-IP rate limit
        if (ipAddress != null && !checkIpLimit(ipAddress)) {
            log.warn("IP rate limit exceeded for IP: {}", ipAddress);
            return false;
        }

        return true;
    }

    /**
     * Check global rate limit
     */
    private boolean checkGlobalLimit() {
        if (globalBucket == null) {
            globalBucket = createGlobalBucket();
        }
        return globalBucket.tryConsume(1);
    }

    /**
     * Check per-user rate limit
     */
    private boolean checkUserLimit(String userId) {
        LocalBucket bucket = userBuckets.computeIfAbsent(userId, k -> createUserBucket());
        return bucket.tryConsume(1);
    }

    /**
     * Check per-IP rate limit
     */
    private boolean checkIpLimit(String ipAddress) {
        LocalBucket bucket = ipBuckets.computeIfAbsent(ipAddress, k -> createIpBucket());
        return bucket.tryConsume(1);
    }

    /**
     * Create global rate limit bucket
     */
    private LocalBucket createGlobalBucket() {
        EmailProperties.LimitConfig globalConfig = emailProperties.getRateLimit().getGlobal();
        
        Bandwidth limit = Bandwidth.classic(
                globalConfig.getBurstCapacity(),
                Refill.intervally(
                        globalConfig.getRequestsPerMinute(),
                        Duration.ofMinutes(1)
                )
        );

        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit)
                .build();

        log.info("Created global rate limit bucket: {} requests/min, burst: {}",
                globalConfig.getRequestsPerMinute(), globalConfig.getBurstCapacity());

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create per-user rate limit bucket
     */
    private LocalBucket createUserBucket() {
        EmailProperties.LimitConfig userConfig = emailProperties.getRateLimit().getPerUser();
        
        Bandwidth limit = Bandwidth.classic(
                userConfig.getBurstCapacity(),
                Refill.intervally(
                        userConfig.getRequestsPerMinute(),
                        Duration.ofMinutes(1)
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create per-IP rate limit bucket
     */
    private LocalBucket createIpBucket() {
        EmailProperties.LimitConfig ipConfig = emailProperties.getRateLimit().getPerIp();
        
        Bandwidth limit = Bandwidth.classic(
                ipConfig.getBurstCapacity(),
                Refill.intervally(
                        ipConfig.getRequestsPerMinute(),
                        Duration.ofMinutes(1)
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get remaining tokens for user
     */
    public long getRemainingTokensForUser(String userId) {
        LocalBucket bucket = userBuckets.get(userId);
        if (bucket == null) {
            return emailProperties.getRateLimit().getPerUser().getBurstCapacity();
        }
        return bucket.getAvailableTokens();
    }

    /**
     * Get remaining tokens for IP
     */
    public long getRemainingTokensForIp(String ipAddress) {
        LocalBucket bucket = ipBuckets.get(ipAddress);
        if (bucket == null) {
            return emailProperties.getRateLimit().getPerIp().getBurstCapacity();
        }
        return bucket.getAvailableTokens();
    }

    /**
     * Reset rate limits for user
     */
    public void resetUserLimit(String userId) {
        userBuckets.remove(userId);
        log.info("Reset rate limit for userId: {}", userId);
    }

    /**
     * Reset rate limits for IP
     */
    public void resetIpLimit(String ipAddress) {
        ipBuckets.remove(ipAddress);
        log.info("Reset rate limit for IP: {}", ipAddress);
    }

    /**
     * Clear all rate limit data
     */
    public void clearAllLimits() {
        userBuckets.clear();
        ipBuckets.clear();
        globalBucket = null;
        log.info("Cleared all rate limit data");
    }
}