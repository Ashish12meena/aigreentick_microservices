package com.aigreentick.services.notification.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aigreentick.services.notification.service.ratelimit.EmailRateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final EmailRateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        if (!request.getRequestURI().contains("/api/v1/notification/email")) {
            return true;
        }

        String userId = request.getHeader("X-User-Id");
        String ipAddress = getClientIpAddress(request);

        boolean allowed = rateLimiterService.allowEmail(userId, ipAddress);

        if (!allowed) {
            log.warn("Rate limit exceeded for userId: {}, IP: {}", userId, ipAddress);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            return false;
        }

        // Add rate limit info to headers
        if (userId != null) {
            long remainingTokens = rateLimiterService.getRemainingTokensForUser(userId);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}