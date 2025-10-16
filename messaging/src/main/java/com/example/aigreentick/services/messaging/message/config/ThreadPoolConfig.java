package com.example.aigreentick.services.messaging.message.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "whatsappExecutor")
    public ExecutorService whatsappExecutor() {
        return Executors.newFixedThreadPool(80); // WhatsApp API limit
    }

    @Bean(name = "broadcastExecutor")
    public ExecutorService broadcastExecutor() {
        return Executors.newFixedThreadPool(10); // or use ThreadPoolExecutor for tuning
    }
}

