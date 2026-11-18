package io.github.sajuu730.guardian.incident;

import io.github.sajuu730.guardian.config.GuardianProperties;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Publishes structured incidents to a webhook with bounded synchronous retries.
 * This deliberately does not claim durable delivery: a process crash can still
 * lose an incident until an outbox is introduced.
 */
@Component
public class LoggingIncidentPublisher implements IncidentPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingIncidentPublisher.class);
    private final RestClient client;
    private final String webhookUrl;
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;

    @Autowired
    public LoggingIncidentPublisher(RestClient.Builder builder, GuardianProperties properties) {
        this(builder, properties, true);
    }

    LoggingIncidentPublisher(RestClient.Builder builder, GuardianProperties properties, boolean configureTimeouts) {
        GuardianProperties.Incidents config = properties.getIncidents();
        this.webhookUrl = config.getWebhookUrl();
        this.maxAttempts = config.getMaxAttempts();
        this.initialBackoff = config.getInitialBackoff();
        this.maxBackoff = config.getMaxBackoff();
        if (maxAttempts < 1 || initialBackoff.isNegative() || maxBackoff.isNegative()
                || config.getRequestTimeout().isNegative() || config.getRequestTimeout().isZero()) {
            throw new IllegalArgumentException("Invalid incident webhook retry or timeout configuration");
        }
        if (configureTimeouts) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(config.getRequestTimeout());
            factory.setReadTimeout(config.getRequestTimeout());
            builder.requestFactory(factory);
        }
        this.client = builder.build();
    }

    @Override
    public void publish(IncidentEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("connect_incident connector={} task={} state={} rootCause={} action={} detail={}",
                    event.connector(), event.taskId(), event.state(), event.rootCause(),
                    event.action(), event.detail());
            return;
        }

        RestClientException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                client.post().uri(webhookUrl).body(event).retrieve().toBodilessEntity();
                return;
            } catch (RestClientException failure) {
                lastFailure = failure;
                if (attempt == maxAttempts) break;
                Duration delay = retryDelay(attempt);
                log.warn("Incident webhook attempt {}/{} failed; retrying in {} ms",
                        attempt, maxAttempts, delay.toMillis(), failure);
                sleep(delay);
            }
        }
        throw new IllegalStateException("Incident webhook delivery failed after " + maxAttempts + " attempt(s)",
                lastFailure);
    }

    private Duration retryDelay(int failedAttempt) {
        long multiplier = 1L << Math.min(failedAttempt - 1, 20);
        long initial = initialBackoff.toMillis();
        long candidate = initial > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : initial * multiplier;
        return Duration.ofMillis(Math.min(candidate, maxBackoff.toMillis()));
    }

    private static void sleep(Duration delay) {
        if (delay.isZero()) return;
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying incident webhook delivery", interrupted);
        }
    }
}
