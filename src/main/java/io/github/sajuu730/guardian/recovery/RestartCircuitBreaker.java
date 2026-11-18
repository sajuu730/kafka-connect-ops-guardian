package io.github.sajuu730.guardian.recovery;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Per-task restart budget. A budget is consumed before the restart request is sent,
 * preventing concurrent poll cycles from exceeding the configured limit.
 */
@Component
public class RestartCircuitBreaker {
    private final Clock clock;
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastAttempt = new ConcurrentHashMap<>();

    public RestartCircuitBreaker() { this(Clock.systemUTC()); }
    RestartCircuitBreaker(Clock clock) { this.clock = clock; }

    public synchronized Decision acquire(String key, int maxRestarts, Duration window, Duration cooldown) {
        Instant now = clock.instant();
        Deque<Instant> history = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        Instant cutoff = now.minus(window);
        while (!history.isEmpty() && history.peekFirst().isBefore(cutoff)) history.removeFirst();

        Instant previous = lastAttempt.get(key);
        if (previous != null && previous.plus(cooldown).isAfter(now))
            return Decision.COOLDOWN;
        if (history.size() >= maxRestarts) return Decision.BUDGET_EXHAUSTED;

        history.addLast(now);
        lastAttempt.put(key, now);
        return Decision.ALLOWED;
    }

    public enum Decision { ALLOWED, COOLDOWN, BUDGET_EXHAUSTED }
}
