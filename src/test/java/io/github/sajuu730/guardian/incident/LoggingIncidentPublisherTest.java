package io.github.sajuu730.guardian.incident;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.sajuu730.guardian.config.GuardianProperties;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LoggingIncidentPublisherTest {
    private IncidentEvent event() {
        return new IncidentEvent(Instant.parse("2026-01-01T00:00:00Z"), "orders", 0,
                "FAILED", null, "connection refused", "RECOVERY_DISABLED", "test incident");
    }

    @Test
    void retriesTransientServerFailureAndDeliversIncident() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GuardianProperties properties = new GuardianProperties();
        properties.getIncidents().setWebhookUrl("http://webhook.test/incidents");
        properties.getIncidents().setMaxAttempts(2);
        properties.getIncidents().setInitialBackoff(Duration.ZERO);
        LoggingIncidentPublisher publisher = new LoggingIncidentPublisher(builder, properties, false);

        server.expect(requestTo("http://webhook.test/incidents"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        server.expect(requestTo("http://webhook.test/incidents"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        publisher.publish(event());
        server.verify();
    }

    @Test
    void surfacesFailureAfterConfiguredAttemptsAreExhausted() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GuardianProperties properties = new GuardianProperties();
        properties.getIncidents().setWebhookUrl("http://webhook.test/incidents");
        properties.getIncidents().setMaxAttempts(2);
        properties.getIncidents().setInitialBackoff(Duration.ZERO);
        LoggingIncidentPublisher publisher = new LoggingIncidentPublisher(builder, properties, false);

        server.expect(requestTo("http://webhook.test/incidents")).andRespond(withServerError());
        server.expect(requestTo("http://webhook.test/incidents")).andRespond(withServerError());

        assertThrows(IllegalStateException.class, () -> publisher.publish(event()));
        server.verify();
    }
}
