package io.github.sajuu730.guardian.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RestartCircuitBreakerTest {
    @Test void enforcesRestartBudgetAndCooldown() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        RestartCircuitBreaker breaker = new RestartCircuitBreaker(clock);
        assertEquals(RestartCircuitBreaker.Decision.ALLOWED,
                breaker.acquire("orders/0", 2, Duration.ofMinutes(15), Duration.ZERO));
        assertEquals(RestartCircuitBreaker.Decision.ALLOWED,
                breaker.acquire("orders/0", 2, Duration.ofMinutes(15), Duration.ZERO));
        assertEquals(RestartCircuitBreaker.Decision.BUDGET_EXHAUSTED,
                breaker.acquire("orders/0", 2, Duration.ofMinutes(15), Duration.ZERO));
    }

    @Test void enforcesCooldown() {
        RestartCircuitBreaker breaker = new RestartCircuitBreaker(
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        assertEquals(RestartCircuitBreaker.Decision.ALLOWED,
                breaker.acquire("orders/1", 3, Duration.ofMinutes(15), Duration.ofMinutes(2)));
        assertEquals(RestartCircuitBreaker.Decision.COOLDOWN,
                breaker.acquire("orders/1", 3, Duration.ofMinutes(15), Duration.ofMinutes(2)));
    }
}
