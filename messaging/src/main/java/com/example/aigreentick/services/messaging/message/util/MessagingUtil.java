package com.example.aigreentick.services.messaging.message.util;


import java.util.ArrayList;
import java.util.List;

public class MessagingUtil {
    
    /**
     * Splits a list into smaller sublists ("chunks") of the given size.
     *
     * @param data      the original list to be chunked
     * @param chunkSize the maximum size of each chunk
     * @param <T>       the type of elements in the list
     * @return a list of sublists (chunks)
     * @throws IllegalArgumentException if chunkSize <= 0
     */
    public static <T> List<List<T>> chunkList(List<T> data, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than 0");
        }

        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < data.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, data.size());
            chunks.add(data.subList(i, end));
        }
        return chunks;
    }
}
