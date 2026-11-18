# Delivery Roadmap

This roadmap distinguishes implemented behavior from items that need additional infrastructure or validation.

## Milestone 1 — Service foundation
- [x] Spring Boot application, typed configuration, health and Prometheus endpoints.
- [x] Safe defaults: recovery disabled until explicitly enabled.
- [x] Maven verification and container-build workflow defined (first successful Actions run still pending).

## Milestone 2 — Connect cluster observation
- [x] Poll connector inventory and connector/task status.
- [x] Bound HTTP timeouts and isolate individual connector inspection failures.
- [x] Detect FAILED tasks and track their first observed unhealthy time.
- [ ] Stalled-task detection using an explicit connector progress signal or Prometheus/JMX integration. The standard Connect status API alone is insufficient.

## Milestone 3 — Diagnosis and recovery
- [x] Classify common failure signatures without discarding the original trace.
- [x] Restart failed tasks only when recovery is enabled.
- [x] Rolling-window restart budget and cooldown per connector/task.
- [x] Record restart-request failures and budget exhaustion as incident actions.
- [ ] Persist restart budgets in a shared store before supporting multiple Guardian replicas with recovery enabled.
- [ ] Verify recovery completion from task state transitions and expose a dedicated recovery outcome metric.

## Milestone 4 — Incidents and observability
- [x] Structured incident payloads with connector, task, state, classification, trace and recovery decision.
- [x] Prometheus counters and unhealthy-duration gauge.
- [x] Bounded synchronous webhook retries with exponential backoff and configurable request timeout.
- [x] Tests for webhook retry success/exhaustion and monitor isolation.
- [ ] Durable asynchronous outbox, delivery authentication/signing and delivery-lag metrics.

## Milestone 5 — Delivery and operations
- [x] Docker image and reproducible local Compose environment.
- [x] Kubernetes Deployment/Service/ConfigMap and operational documentation.
- [x] CI workflow for Maven verification and Docker image build.
- [ ] Successful GitHub Actions run and live Kafka Connect integration validation.
- [ ] Publish versioned container images and replace the Kubernetes image placeholder.

## First-release constraints

- Restart budgets are process-local; run one Guardian replica while automated recovery is enabled.
- A restart request is not proof of recovery; verify task state in a later poll.
- Webhook delivery is synchronous and not durable across process crashes.
- Stalled-task detection requires an explicit progress metric; RUNNING status alone is not evidence of progress.
- Guardian does not replace Kafka Connect's distributed coordination, restart whole connectors, or mutate connector configuration.
