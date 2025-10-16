package com.aigreentick.services.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    // Single shared ObjectMapper (thread-safe)
     private static final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // ✅ snake_case
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)        // skip nulls
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private JsonUtils() {
        // Prevent instantiation
    }

    /**
     * Serialize object to JSON string (ignores null fields).
     */
    public static <T> String serializeToString(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL) // ✅ applied only here
                    .writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON: {}", obj, e);
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    /**
     * Deserialize JSON string to object.
     */
    public static <T> T deserializeToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), json, e);
            throw new IllegalStateException("JSON deserialization failed", e);
        }
    }

    /**
     * Pretty-print object to console.
     */
    public static void printPretty(Object obj) {
        try {
            String prettyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to pretty print object", e);
        }
    }

    /**
     * Convert object to pretty JSON string.
     */
    public static String toPrettyString(Object obj) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to pretty string", e);
        }
    }
}
