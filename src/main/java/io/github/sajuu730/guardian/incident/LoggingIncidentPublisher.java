package io.github.sajuu730.guardian.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingIncidentPublisher implements IncidentPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingIncidentPublisher.class);
    @Override
    public void publish(IncidentEvent event) {
        log.warn("connect_incident connector={} task={} state={} rootCause={} action={} detail={}",
                event.connector(), event.taskId(), event.state(), event.rootCause(),
                event.action(), event.detail());
    }
}
