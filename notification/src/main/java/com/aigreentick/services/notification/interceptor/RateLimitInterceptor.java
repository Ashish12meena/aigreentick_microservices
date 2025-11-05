package com.aigreentick.services.notification.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aigreentick.services.notification.service.ratelimit.RedisRateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor for distributed rate limiting using Redis
 * Works seamlessly across multiple service instances
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        // Only apply to email notification endpoints
        if (!request.getRequestURI().contains("/api/v1/notification/email")) {
            return true;
        }

        String userId = request.getHeader("X-User-Id");
        String ipAddress = getClientIpAddress(request);

        log.debug("Rate limit check - userId: {}, IP: {}, URI: {}", 
                userId, ipAddress, request.getRequestURI());

        boolean allowed = rateLimiterService.allowEmail(userId, ipAddress);

        if (!allowed) {
            log.warn("Rate limit exceeded - userId: {}, IP: {}, URI: {}", 
                    userId, ipAddress, request.getRequestURI());
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Rate limit exceeded\"," +
                    "\"message\":\"Too many requests. Please try again later.\"," +
                    "\"code\":\"RATE_LIMIT_EXCEEDED\"}");
            return false;
        }

        // Add rate limit info to response headers
        addRateLimitHeaders(response, userId, ipAddress);

        return true;
    }

    /**
     * Add rate limit information to response headers
     */
    private void addRateLimitHeaders(HttpServletResponse response, String userId, String ipAddress) {
        try {
            if (userId != null) {
                long remainingTokens = rateLimiterService.getRemainingTokensForUser(userId);
                response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                response.setHeader("X-RateLimit-Type", "user");
                response.setHeader("X-RateLimit-Identifier", userId);
            } else if (ipAddress != null) {
                long remainingTokens = rateLimiterService.getRemainingTokensForIp(ipAddress);
                response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                response.setHeader("X-RateLimit-Type", "ip");
                response.setHeader("X-RateLimit-Identifier", ipAddress);
            }
            
            response.setHeader("X-RateLimit-Window", "60s");
            
        } catch (Exception e) {
            log.error("Error adding rate limit headers", e);
            // Don't fail the request if header addition fails
        }
    }

    /**
     * Get client IP address from request headers or remote address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (for requests behind proxy/load balancer)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one (client IP)
            String clientIp = xForwardedFor.split(",")[0].trim();
            log.debug("Client IP from X-Forwarded-For: {}", clientIp);
            return clientIp;
        }
        
        // Check X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            log.debug("Client IP from X-Real-IP: {}", xRealIp);
            return xRealIp;
        }
        
        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP from remote address: {}", remoteAddr);
        return remoteAddr;
    }
}