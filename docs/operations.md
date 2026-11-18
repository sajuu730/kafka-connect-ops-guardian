# Operations Guide

## Run locally

Build and test:

```bash
mvn --batch-mode verify
docker build -t kafka-connect-ops-guardian .
```

Start the demo stack:

```bash
docker compose up --build
```

Check the service:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8083/connectors
```

## Incident webhook delivery

Set `GUARDIAN_WEBHOOK_URL` to an HTTPS endpoint reachable by Guardian. Configure:
- `GUARDIAN_WEBHOOK_TIMEOUT` (default `3s`): connect/read timeout for each attempt
- `GUARDIAN_WEBHOOK_MAX_ATTEMPTS` (default `3`): total attempts, including the first request
- `GUARDIAN_WEBHOOK_INITIAL_BACKOFF` (default `250ms`): first retry delay
- `GUARDIAN_WEBHOOK_MAX_BACKOFF` (default `2s`): upper bound for exponential retry delay

Webhook retries are synchronous and bounded. A slow endpoint can delay the polling thread, and a process crash can lose an incident. Use an authenticated HTTPS endpoint and do not commit credentials. A durable outbox and delivery authentication are still required for production-grade delivery guarantees.

## Enable recovery carefully

The default is `GUARDIAN_RECOVERY_ENABLED=false`. Before enabling recovery:

1. Test against a disposable Kafka Connect cluster.
2. Confirm the Connect REST API is reachable only by trusted workloads.
3. Configure a webhook or ensure incident logs are collected.
4. Set a conservative restart budget and cooldown.
5. Start with one Guardian replica. The restart budget is currently process-local.
6. Review logs and Prometheus metrics after deliberately failing a test connector.

A restart counter records accepted restart requests, not successful task recovery. Confirm recovery on a later poll through the Connect status API.

## Kubernetes

Apply the ConfigMap, then Deployment and Service:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
```

The example image name is a placeholder until a tagged image is published. Configure the image tag and Connect URL for your cluster. Recovery remains disabled in the example. The Deployment reads non-secret settings from the ConfigMap and optionally reads `GUARDIAN_WEBHOOK_URL` from the `kafka-connect-ops-guardian-secrets` Secret.

Create the Secret only when a webhook is configured; do not commit it:

```bash
kubectl create secret generic kafka-connect-ops-guardian-secrets \
  --from-literal=webhook-url='https://your-internal-webhook.example/incidents'
```

## Security notes

The demo Compose stack is for local development only. Do not expose Kafka Connect's REST API publicly. Use TLS, authentication, network policies, secret references, and restricted RBAC in production. Never place webhook credentials in a committed ConfigMap.
