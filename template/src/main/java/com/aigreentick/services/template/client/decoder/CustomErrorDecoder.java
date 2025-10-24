package com.aigreentick.services.template.client.decoder;

import com.aigreentick.services.common.exceptions.client.ResourceNotFoundException;
import com.aigreentick.services.common.exceptions.infrastructure.ExternalServiceException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder{

    @Override
    public Exception decode(String methodKey, Response response) {
         switch (response.status()) {
            case 404:
                return new ResourceNotFoundException("Resource not found");
            case 503:
                return new ExternalServiceException("Service unavailable");
            default:
                return new RuntimeException("Client error occurred");
        }
    }
    
}
