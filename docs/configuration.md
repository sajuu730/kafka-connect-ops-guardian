# Configuration

The service uses Spring Boot externalized configuration. Environment variables shown below override the defaults in `application.yml`.

| Variable | Default | Purpose |
|---|---:|---|
| `SERVER_PORT` | `8080` | HTTP server port |
| `CONNECT_URL` | `http://localhost:8083` | Kafka Connect Distributed REST base URL |
| `CONNECT_REQUEST_TIMEOUT` | `5s` | Per-request Connect REST timeout |
| `GUARDIAN_POLLING_ENABLED` | `true` | Enable scheduled observation |
| `GUARDIAN_POLL_INTERVAL` | `15s` | Poll interval |
| `GUARDIAN_STALLED_AFTER` | `2m` | Reserved threshold; stalled-task detection is not implemented without a progress signal |
| `GUARDIAN_RECOVERY_ENABLED` | `false` | Explicit opt-in to automated task restarts |
| `GUARDIAN_MAX_RESTARTS` | `3` | Restart budget in the rolling window |
| `GUARDIAN_RESTART_WINDOW` | `15m` | Rolling restart budget window |
| `GUARDIAN_RESTART_COOLDOWN` | `2m` | Minimum time between restart attempts |
| `GUARDIAN_WEBHOOK_URL` | empty | Incident webhook endpoint; use HTTPS |
| `GUARDIAN_WEBHOOK_TIMEOUT` | `3s` | Connect/read timeout for each webhook attempt |
| `GUARDIAN_WEBHOOK_MAX_ATTEMPTS` | `3` | Total webhook attempts, including the first |
| `GUARDIAN_WEBHOOK_INITIAL_BACKOFF` | `250ms` | Initial exponential retry delay |
| `GUARDIAN_WEBHOOK_MAX_BACKOFF` | `2s` | Maximum webhook retry delay |

Recovery is disabled by default. Enable it only after validating the monitor against a non-production Connect cluster and configuring an escalation destination. Restart budgets are process-local, so run a single Guardian replica while recovery is enabled.

Webhook retries are synchronous and bounded. There is no durable outbox; a process crash or prolonged webhook outage can still lose an incident. Protect both the Connect REST API and webhook endpoint with network policy and authentication.
