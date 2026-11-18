# Kafka Connect Ops Guardian

**An operational reliability service for distributed Kafka Connect clusters.**

Kafka Connect Ops Guardian inspects the Kafka Connect Distributed REST API, identifies failed tasks, classifies common failure causes, and coordinates safe, rate-limited recovery. It exposes operational metrics and sends structured incidents to a configurable webhook.

> Early-stage implementation. Automated recovery is disabled by default; review the limitations before enabling it.

## Capabilities implemented

- Spring Boot service foundation and externalized configuration.
- Scheduled polling of `/connectors` and `/connectors/{name}/status`.
- FAILED task detection and trace-based root-cause classification.
- Per-task rolling-window restart limits and cooldown for opt-in task restart.
- Structured incident webhook delivery with bounded exponential retries; structured logging when no webhook is configured.
- Isolation of connector inspection and incident delivery failures so one failure does not stop the rest of a polling cycle.
- Prometheus metrics and Spring Boot Actuator health endpoints.
- Unit tests for classification, REST API calls, monitoring, restart circuit-breaker behavior and webhook retries.
- Dockerfile, local Compose example, Kubernetes Deployment, Service and ConfigMap.
- GitHub Actions workflow for Maven verification and Docker image build.

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
 Rolling Restart Budget     Webhook Retry / Logs
        |
        v
 POST /connectors/{name}/tasks/{id}/restart

                  Micrometer / Prometheus
```

## Safety defaults

Automated recovery is disabled by default. When enabled, a per-task rolling restart budget and cooldown prevent unbounded restart loops. The service records an accepted restart request; it does not treat that request as proof the task recovered.

## Important limitations

- Stalled-task detection is not implemented. Kafka Connect's standard status API reports state and trace, not record-level progress. A reliable progress signal must be supplied by connector-specific metrics or another monitoring integration.
- Restart budgets are held in process memory. Use one Guardian replica while recovery is enabled; distributed coordination is required before active-active recovery.
- Webhook retries are synchronous and bounded. There is no durable outbox, so a process crash or prolonged endpoint outage can still lose an incident.
- Webhook authentication/signing and credential rotation are deployment responsibilities, not built-in features.
- The first version detects FAILED tasks. It does not automatically restart entire connectors or alter connector configuration.
- Root-cause classification is heuristic and should be treated as diagnostic assistance, not a definitive diagnosis.
- The Kubernetes image reference is a placeholder until a container is published to GHCR.

## Technology

Java 21, Spring Boot, Kafka Connect REST API, Micrometer, Prometheus, Docker, Kubernetes, JUnit 5.

## Local development

Requirements: Java 21 and Maven 3.9+.

```bash
mvn --batch-mode verify
docker build -t kafka-connect-ops-guardian .
```

To run against a local Connect REST endpoint:

```bash
CONNECT_URL=http://localhost:8083 mvn spring-boot:run
```

Health: `http://localhost:8080/actuator/health`

Prometheus: `http://localhost:8080/actuator/prometheus`

## Configuration and operations

- [Configuration reference](docs/configuration.md)
- [Architecture and limitations](docs/architecture.md)
- [Operations guide](docs/operations.md)
- [Delivery roadmap](docs/roadmap.md)

Never expose the Connect REST API publicly. Use TLS, authentication, network policies and secret references in production.

## License

MIT. See [LICENSE](LICENSE).
