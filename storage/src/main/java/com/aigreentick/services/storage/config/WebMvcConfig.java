package com.aigreentick.services.storage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.aigreentick.services.storage.interceptor.RateLimitInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Registers the UserContextInterceptor with Spring MVC.
 */

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserContextInterceptor userContextInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**") // intercept all routes
                .order(1);

        // Rate limiting interceptor - runs second
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api-docs/**", "/swagger-ui/**", "/actuator/**")
                .order(2);
    }
}
