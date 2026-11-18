# Kafka Connect Ops Guardian

**An operational reliability service for distributed Kafka Connect clusters.**

Kafka Connect Ops Guardian continuously inspects the Kafka Connect Distributed REST API, identifies unhealthy connectors and tasks, classifies failure causes, and coordinates safe, rate-limited recovery. It exposes operational metrics and sends structured incidents to a configurable webhook.

> Status: initial implementation in progress. Features are documented as implemented only when backed by code and tests.

## Why this project exists

Building a connector is only part of operating Kafka Connect. Production operations also require health visibility, failure diagnosis, controlled recovery, escalation, and evidence that automation itself is healthy. Ops Guardian focuses on that operational layer.

## Planned capabilities

- [x] Spring Boot service foundation and externalized configuration
- [ ] Scheduled polling of `/connectors` and connector/task status endpoints
- [ ] FAILED-state and configurable stalled-task detection
- [ ] Root-cause classification from Connect trace/error details
- [ ] Controlled task restart with rolling-window circuit breaker
- [ ] Idempotent recovery coordination and cooldown
- [ ] Prometheus metrics and health endpoints
- [ ] Structured incident webhook delivery
- [ ] Unit and integration tests with WireMock/Testcontainers
- [ ] Docker image and Compose demo environment
- [ ] Kubernetes Deployment, Service, ConfigMap and CronJob examples
- [ ] GitHub Actions CI

## Architecture

```text
Kafka Connect REST API
        |
        v
 Polling / Status Collector
        |
        v
 Health Evaluation ----> Root-cause Classifier
        |                         |
        v                         v
 Recovery Coordinator       Incident Event
        |                         |
        v                         v
 Circuit Breaker             Webhook Sender
        |
        v
 POST /connectors/{name}/tasks/{id}/restart

                  Micrometer / Prometheus
                           |
                           v
                     Grafana (demo)
```

## Safety model

Automated recovery is deliberately conservative:

1. Only configured, eligible failures are considered for restart.
2. A per-task rolling restart limit prevents restart loops.
3. A cooldown prevents repeated actions for the same observed failure.
4. When the limit is reached, automation stops and emits an escalation incident.
5. The service never claims a restart succeeded until Kafka Connect confirms the request was accepted.
6. Webhook delivery failure is observable and does not silently erase the incident.

A task restart is not proof that the underlying cause is fixed. Operators remain responsible for investigating persistent incidents.

## Local development

Requirements: Java 21 and Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

The service listens on port `8080`. Actuator health is available at `/actuator/health`; Prometheus metrics are exposed at `/actuator/prometheus` when enabled.

## Configuration

All settings use environment variables or Spring configuration. Never commit credentials or production endpoints. See [docs/configuration.md](docs/configuration.md) as configuration is implemented.

## Project quality

- Java 21, Spring Boot, Maven
- JUnit 5 and Mockito
- Docker and Kubernetes deployment examples
- CI validation on pull requests and pushes

## Roadmap

See [docs/roadmap.md](docs/roadmap.md) for milestones and known limitations.

## License

MIT (see [LICENSE](LICENSE)).
