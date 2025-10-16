package com.aigreentick.services.template.service.impl;

import java.time.Duration;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class TemplateMetricsService {

    private final Counter templatesCreatedCounter;
    private final Counter templatesSentCounter;
    private final Counter templatesFailedCounter;
    private final Timer templateProcessingTimer;
    private final DistributionSummary templateSizeSummary;

    public TemplateMetricsService(MeterRegistry meterRegistry) {
        // Counter for total templates created
        templatesCreatedCounter = Counter.builder("template.created.total")
                .description("Total number of templates created")
                .tags("service", "template")
                .register(meterRegistry);

        // Counter for total templates successfully sent
        templatesSentCounter = Counter.builder("template.sent.total")
                .description("Total number of templates successfully sent")
                .tags("service", "template")
                .register(meterRegistry);

        // Counter for failed template sends
        templatesFailedCounter = Counter.builder("template.failed.total")
                .description("Total number of templates failed to send")
                .tags("service", "template")
                .register(meterRegistry);

        // Timer for template processing durations
        templateProcessingTimer = Timer.builder("template.processing.time")
                .description("Time taken to process template requests")
                .tags("service", "template")
                .publishPercentiles(0.5, 0.95, 0.99) // median, 95th, 99th
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(meterRegistry);

        // Distribution summary for template payload size (in bytes)
        templateSizeSummary = DistributionSummary.builder("template.size.bytes")
                .description("Distribution of template payload sizes")
                .baseUnit("bytes")
                .tags("service", "template")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /** Call when a template is created */
    public void incrementTemplatesCreated() {
        templatesCreatedCounter.increment();
    }

    /** Call when a template is successfully sent */
    public void incrementTemplatesSent() {
        templatesSentCounter.increment();
    }

    /** Call when a template send fails */
    public void incrementTemplatesFailed() {
        templatesFailedCounter.increment();
    }

    /** Call to record template processing duration in milliseconds */
    public void recordTemplateProcessingTime(long millis) {
        templateProcessingTimer.record(Duration.ofMillis(millis));
    }

    /** Call to record template payload size in bytes */
    public void recordTemplateSize(int bytes) {
        templateSizeSummary.record(bytes);
    }

}
