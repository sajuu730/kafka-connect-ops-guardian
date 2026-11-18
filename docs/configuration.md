# Configuration

The service uses Spring Boot externalized configuration. Environment variables shown below override the defaults in `application.yml`.

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP server port |
| `CONNECT_URL` | `http://localhost:8083` | Kafka Connect Distributed REST base URL |
| `CONNECT_REQUEST_TIMEOUT` | `5s` | Per-request timeout |
| `GUARDIAN_POLLING_ENABLED` | `true` | Enable scheduled observation |
| `GUARDIAN_POLL_INTERVAL` | `15s` | Poll interval |
| `GUARDIAN_STALLED_AFTER` | `2m` | Threshold for stalled-task detection |
| `GUARDIAN_RECOVERY_ENABLED` | `false` | Explicit opt-in to automated restarts |
| `GUARDIAN_MAX_RESTARTS` | `3` | Restart budget in the rolling window |
| `GUARDIAN_RESTART_WINDOW` | `15m` | Rolling restart budget window |
| `GUARDIAN_RESTART_COOLDOWN` | `2m` | Minimum time between restart attempts |
| `GUARDIAN_WEBHOOK_URL` | empty | Incident webhook endpoint |

Recovery is disabled by default. Enable it only after validating the monitor against a non-production Connect cluster and configuring an escalation destination. Use network policy and authentication to protect the Connect REST API and webhook endpoint.
