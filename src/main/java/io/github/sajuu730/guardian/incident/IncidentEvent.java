package io.github.sajuu730.guardian.incident;

import java.time.Instant;
import io.github.sajuu730.guardian.diagnosis.RootCause;

public record IncidentEvent(
        Instant occurredAt,
        String connector,
        Integer taskId,
        String state,
        RootCause rootCause,
        String trace,
        String action,
        String detail) { }
