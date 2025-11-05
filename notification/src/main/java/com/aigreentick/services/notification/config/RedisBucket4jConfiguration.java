package com.aigreentick.services.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Bucket4j with Redis (Lettuce client)
 */
@Slf4j
@Configuration
public class RedisBucket4jConfiguration {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * Create Lettuce Redis client for Bucket4j
     */
    @Bean
    public RedisClient redisClient() {
        RedisURI.Builder uriBuilder = RedisURI.Builder
                .redis(redisHost, redisPort)
                .withDatabase(redisDatabase);
        
        // Add password if configured
        if (redisPassword != null && !redisPassword.isEmpty()) {
            uriBuilder.withPassword(redisPassword.toCharArray());
        }
        
        RedisURI redisUri = uriBuilder.build();
        
        RedisClient client = RedisClient.create(redisUri);
        
        log.info("Redis client created for Bucket4j: {}:{}", redisHost, redisPort);
        
        return client;
    }
}