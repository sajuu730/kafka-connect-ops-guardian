# Demo capture guide

This guide describes reproducible evidence to capture from a real local run. Screenshots and recordings should be generated from the running services, not mockups.

## Kafka Connect Toolkit

Start the local stack using the README quick-start commands, then capture:

1. Connector inventory: Kafka Connect GET /connector-plugins showing the custom REST source and JDBC sink plugins.
2. Pipeline result: a source record visible in Kafka and the corresponding row visible in MySQL.
3. DLQ behavior: a deliberately malformed record routed to the configured dead-letter topic.
4. Observability: the provisioned Grafana dashboard showing live metrics from the local stack.
5. CI evidence: GitHub Actions run showing both verify and end-to-end jobs green.

Suggested filenames under docs/assets/: toolkit-connectors.png, toolkit-pipeline.png, toolkit-dlq.png, toolkit-grafana.png, toolkit-ci.png.

## Kafka Connect Ops Guardian

Start the Compose stack and capture:

1. Healthy baseline: Guardian Actuator health endpoint and the monitored Connect REST API.
2. Failed-task diagnosis: a test connector/task intentionally configured to fail, with the resulting incident classification in Guardian logs or webhook receiver.
3. Recovery guard: recovery disabled by default, then a controlled test with recovery explicitly enabled and the restart budget/cooldown visible in logs or metrics.
4. Prometheus: the Guardian metrics endpoint showing observation and restart metrics.
5. CI evidence: successful Maven verification and Docker image build.

Suggested filenames: guardian-healthy.png, guardian-incident.png, guardian-recovery-guard.png, guardian-metrics.png, guardian-ci.png.

## Short demo recording

Record a 60–90 second walkthrough:
- 0–15s: show the repository and architecture diagram.
- 15–35s: show the running service and connector/task state.
- 35–60s: demonstrate a real pipeline or controlled failed-task incident.
- 60–80s: show the relevant metrics and green CI workflow.

Do not expose passwords, bearer tokens, webhook secrets, private endpoints or personal information. Do not label a screenshot as a live production deployment unless it actually is one.
