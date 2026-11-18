package io.github.sajuu730.guardian.diagnosis;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RootCauseClassifier {
    public RootCause classify(String trace) {
        if (trace == null || trace.isBlank()) return RootCause.UNKNOWN;
        String value = trace.toLowerCase(Locale.ROOT);
        if (contains(value, "authentication", "sasl", "invalid credentials", "login failed"))
            return RootCause.AUTHENTICATION;
        if (contains(value, "authorization", "forbidden", "not authorized", "acl"))
            return RootCause.AUTHORIZATION;
        if (contains(value, "too many requests", "http 429", "rate limit"))
            return RootCause.RATE_LIMITED;
        if (contains(value, "serialization", "deserialization", "schema registry", "avro"))
            return RootCause.SERIALIZATION;
        if (contains(value, "connection refused", "unknownhost", "timed out", "timeout", "network"))
            return RootCause.NETWORK;
        if (contains(value, "sql", "jdbc", "database", "postgres", "mysql"))
            return RootCause.DATABASE;
        if (contains(value, "configuration", "invalid config", "configexception"))
            return RootCause.CONFIGURATION;
        return RootCause.UNKNOWN;
    }

    private boolean contains(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }
}
