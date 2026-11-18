# Delivery Roadmap

## Milestone 1 — Service foundation
- Spring Boot application, typed configuration, health and Prometheus endpoints.
- Safe defaults: recovery disabled until explicitly enabled.
- CI compiles and runs unit tests.

## Milestone 2 — Connect cluster observation
- Poll connector inventory and connector/task status.
- Bound HTTP timeouts and handle Connect API errors.
- Track last state transition and task progress timestamps.
- Detect FAILED and configurable stalled states.

## Milestone 3 — Diagnosis and recovery
- Classify common failure signatures without discarding the original trace.
- Restart failed tasks only when recovery is enabled.
- Rolling-window restart budget per connector/task.
- Cooldown and duplicate-action protection.
- Escalate when the budget is exhausted.

## Milestone 4 — Incidents and observability
- Structured webhook payloads with cluster, connector, task, state, classification and recovery decision.
- Prometheus counters and duration gauges.
- Tests for webhook success, failure and timeouts.

## Milestone 5 — Delivery and operations
- Docker image and reproducible local Compose environment.
- Kubernetes Deployment/Service/ConfigMap and optional CronJob mode.
- CI container build and security-conscious configuration guidance.
- End-to-end tests against a disposable Kafka Connect environment.

## Non-goals for the first release
- Replacing Kafka Connect's own distributed coordination.
- Restarting every FAILED task unconditionally.
- Claiming exactly-once recovery.
- Automatically changing connector configuration.
