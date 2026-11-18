package io.github.sajuu730.guardian.incident;

import io.github.sajuu730.guardian.config.GuardianProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Publishes structured incidents to a webhook, falling back to structured logs. */
@Component
public class LoggingIncidentPublisher implements IncidentPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingIncidentPublisher.class);
    private final RestClient client;
    private final String webhookUrl;

    public LoggingIncidentPublisher(RestClient.Builder builder, GuardianProperties properties) {
        this.client = builder.build();
        this.webhookUrl = properties.getIncidents().getWebhookUrl();
    }

    @Override
    public void publish(IncidentEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("connect_incident connector={} task={} state={} rootCause={} action={} detail={}",
                    event.connector(), event.taskId(), event.state(), event.rootCause(),
                    event.action(), event.detail());
            return;
        }
        client.post().uri(webhookUrl).body(event).retrieve().toBodilessEntity();
    }
}
