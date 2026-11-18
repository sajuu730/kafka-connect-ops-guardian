package io.github.sajuu730.guardian.monitor;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sajuu730.guardian.config.GuardianProperties;
import io.github.sajuu730.guardian.connect.ConnectApiClient;
import io.github.sajuu730.guardian.diagnosis.RootCause;
import io.github.sajuu730.guardian.diagnosis.RootCauseClassifier;
import io.github.sajuu730.guardian.incident.IncidentEvent;
import io.github.sajuu730.guardian.incident.IncidentPublisher;
import io.github.sajuu730.guardian.recovery.RestartCircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ConnectHealthMonitor {
    private static final Logger log = LoggerFactory.getLogger(ConnectHealthMonitor.class);
    private final ConnectApiClient connect;
    private final GuardianProperties properties;
    private final RootCauseClassifier classifier;
    private final RestartCircuitBreaker breaker;
    private final IncidentPublisher incidents;
    private final Counter restartCounter;
    private final Counter failureCounter;
    private final Map<String, Instant> unhealthySince = new ConcurrentHashMap<>();
    private final AtomicLong unhealthySeconds = new AtomicLong();

    public ConnectHealthMonitor(ConnectApiClient connect, GuardianProperties properties,
            RootCauseClassifier classifier, RestartCircuitBreaker breaker,
            IncidentPublisher incidents, MeterRegistry registry) {
        this.connect = connect;
        this.properties = properties;
        this.classifier = classifier;
        this.breaker = breaker;
        this.incidents = incidents;
        this.restartCounter = Counter.builder("connector_auto_restarts")
                .description("Accepted Kafka Connect task restart requests").register(registry);
        this.failureCounter = Counter.builder("connector_observation_failures")
                .description("Failures while polling Kafka Connect").register(registry);
        Gauge.builder("connector_unhealthy_duration_seconds", unhealthySeconds, AtomicLong::get)
                .description("Duration in seconds of the oldest currently observed unhealthy connector or task")
                .register(registry);
    }

    @Scheduled(fixedDelayString = "${guardian.polling.interval:15s}")
    public void poll() {
        if (!properties.getPolling().isEnabled()) return;
        try {
            for (String connector : connect.listConnectors()) {
                try {
                    inspect(connector);
                } catch (RuntimeException exception) {
                    failureCounter.increment();
                    log.error("Failed to inspect Kafka Connect connector {}", connector, exception);
                }
            }
        } catch (RuntimeException exception) {
            failureCounter.increment();
            log.error("Kafka Connect connector discovery failed", exception);
        } finally {
            updateUnhealthyDuration();
        }
    }

    void inspect(String connector) {
        JsonNode status = connect.connectorStatus(connector);
        if (status == null) return;
        JsonNode connectorStatus = status.path("connector");
        String connectorState = connectorStatus.path("state").asText("UNKNOWN");
        String connectorKey = connector + "/connector";
        if ("FAILED".equalsIgnoreCase(connectorState)) {
            unhealthySince.computeIfAbsent(connectorKey, ignored -> Instant.now());
            String trace = connectorStatus.path("trace").asText("");
            publishIncident(new IncidentEvent(Instant.now(), connector, null, connectorState,
                    classifier.classify(trace), trace, "RECOVERY_DISABLED",
                    "Connector is FAILED; automated task recovery does not apply to connector-level failures."));
        } else {
            unhealthySince.remove(connectorKey);
        }

        JsonNode tasks = status.path("tasks");
        if (!tasks.isArray()) return;

        for (JsonNode task : tasks) {
            int taskId = task.path("id").asInt(-1);
            String state = task.path("state").asText("UNKNOWN");
            String key = connector + "/" + taskId;
            if ("FAILED".equalsIgnoreCase(state)) {
                unhealthySince.computeIfAbsent(key, ignored -> Instant.now());
                String trace = task.path("trace").asText("");
                RootCause rootCause = classifier.classify(trace);
                String action = "RECOVERY_DISABLED";
                String detail = "Task is FAILED; automated recovery is disabled.";
                if (properties.getRecovery().isEnabled()) {
                    RestartCircuitBreaker.Decision decision = breaker.acquire(key,
                            properties.getRecovery().getMaxRestarts(),
                            properties.getRecovery().getWindow(),
                            properties.getRecovery().getCooldown());
                    action = decision.name();
                    detail = switch (decision) {
                        case ALLOWED -> "Restart request accepted by Kafka Connect.";
                        case COOLDOWN -> "Restart suppressed by cooldown.";
                        case BUDGET_EXHAUSTED -> "Restart budget exhausted; operator escalation required.";
                    };
                    if (decision == RestartCircuitBreaker.Decision.ALLOWED) {
                        try {
                            connect.restartTask(connector, taskId);
                            restartCounter.increment();
                        } catch (RuntimeException exception) {
                            action = "RESTART_REQUEST_FAILED";
                            detail = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
                            failureCounter.increment();
                        }
                    }
                }
                publishIncident(new IncidentEvent(Instant.now(), connector, taskId, state,
                        rootCause, trace, action, detail));
            } else {
                unhealthySince.remove(key);
            }
        }
    }

    private void publishIncident(IncidentEvent event) {
        try {
            incidents.publish(event);
        } catch (RuntimeException exception) {
            failureCounter.increment();
            log.error("Failed to publish incident for connector {} task {}",
                    event.connector(), event.taskId(), exception);
        }
    }

    private void updateUnhealthyDuration() {
        long seconds = unhealthySince.values().stream()
                .mapToLong(start -> Duration.between(start, Instant.now()).toSeconds())
                .max().orElse(0L);
        unhealthySeconds.set(seconds);
    }
}
