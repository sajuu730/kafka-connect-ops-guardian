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

## Enable recovery carefully

The default is `GUARDIAN_RECOVERY_ENABLED=false`. Before enabling recovery:

1. Test against a disposable Kafka Connect cluster.
2. Confirm the Connect REST API is reachable only by trusted workloads.
3. Configure a webhook or ensure incident logs are collected.
4. Set a conservative restart budget and cooldown.
5. Start with one Guardian replica. The restart budget is currently process-local.
6. Review logs and Prometheus metrics after deliberately failing a test connector.

## Kubernetes

Apply the ConfigMap, then Deployment and Service:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
```

The example image name is a placeholder until a tagged image is published. Configure the image tag and Connect URL for your cluster. Recovery remains disabled in the example.

## Security notes

The demo Compose stack is for local development only. Do not expose Kafka Connect's REST API publicly. Use TLS, authentication, network policies, secret references, and restricted RBAC in production. Never place webhook credentials in a committed ConfigMap.
