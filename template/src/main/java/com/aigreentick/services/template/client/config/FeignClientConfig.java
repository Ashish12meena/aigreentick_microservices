package com.aigreentick.services.template.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aigreentick.services.template.client.decoder.CustomErrorDecoder;

import feign.codec.ErrorDecoder;

@Configuration
public class FeignClientConfig {
     @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
