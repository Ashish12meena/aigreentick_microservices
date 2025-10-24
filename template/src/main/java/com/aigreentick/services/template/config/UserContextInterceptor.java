package com.aigreentick.services.template.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.context.UserContextData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepts every HTTP request to extract user and organisation context.
 */
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader("X-User-Id");
        String orgIdHeader = request.getHeader("X-Org-Id");

        Long userId = userIdHeader != null ? Long.valueOf(userIdHeader) : null;
        Long orgId = orgIdHeader != null ? Long.valueOf(orgIdHeader) : null;

        log.info("X-User-Id={} X-Org-Id={} | UserContext set userId={} orgId={}",
                userIdHeader, orgIdHeader, userId, orgId);
        UserContextData contextData = new UserContextData(userId, orgId);
        UserContext.set(contextData);

        return true; // continue the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        UserContext.clear();
    }
}
