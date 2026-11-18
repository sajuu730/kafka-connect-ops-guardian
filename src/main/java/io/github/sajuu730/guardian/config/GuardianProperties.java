package io.github.sajuu730.guardian.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guardian")
public class GuardianProperties {
    private final Connect connect = new Connect();
    private final Polling polling = new Polling();
    private final Recovery recovery = new Recovery();
    private final Incidents incidents = new Incidents();

    public Connect getConnect() { return connect; }
    public Polling getPolling() { return polling; }
    public Recovery getRecovery() { return recovery; }
    public Incidents getIncidents() { return incidents; }

    public static class Connect {
        private String baseUrl = "http://localhost:8083";
        private Duration requestTimeout = Duration.ofSeconds(5);
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Duration getRequestTimeout() { return requestTimeout; }
        public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }
    }
    public static class Polling {
        private boolean enabled = true;
        private Duration interval = Duration.ofSeconds(15);
        private Duration stalledAfter = Duration.ofMinutes(2);
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }
        public Duration getStalledAfter() { return stalledAfter; }
        public void setStalledAfter(Duration stalledAfter) { this.stalledAfter = stalledAfter; }
    }
    public static class Recovery {
        private boolean enabled = false;
        private int maxRestarts = 3;
        private Duration window = Duration.ofMinutes(15);
        private Duration cooldown = Duration.ofMinutes(2);
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxRestarts() { return maxRestarts; }
        public void setMaxRestarts(int maxRestarts) { this.maxRestarts = maxRestarts; }
        public Duration getWindow() { return window; }
        public void setWindow(Duration window) { this.window = window; }
        public Duration getCooldown() { return cooldown; }
        public void setCooldown(Duration cooldown) { this.cooldown = cooldown; }
    }
    public static class Incidents {
        private String webhookUrl = "";
        private Duration requestTimeout = Duration.ofSeconds(3);
        private int maxAttempts = 3;
        private Duration initialBackoff = Duration.ofMillis(250);
        private Duration maxBackoff = Duration.ofSeconds(2);
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public Duration getRequestTimeout() { return requestTimeout; }
        public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public Duration getInitialBackoff() { return initialBackoff; }
        public void setInitialBackoff(Duration initialBackoff) { this.initialBackoff = initialBackoff; }
        public Duration getMaxBackoff() { return maxBackoff; }
        public void setMaxBackoff(Duration maxBackoff) { this.maxBackoff = maxBackoff; }
    }
}
