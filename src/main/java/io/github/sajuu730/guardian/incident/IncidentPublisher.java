package io.github.sajuu730.guardian.incident;

public interface IncidentPublisher {
    void publish(IncidentEvent event);
}
