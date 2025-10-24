package com.aigreentick.services.template.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Registers the UserContextInterceptor with Spring MVC.
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
     private final UserContextInterceptor userContextInterceptor;

    @Autowired
    public WebMvcConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**"); // intercept all routes
    }
}
