# webhook-processor

Service that processes incoming webhooks from partners (Stripe, TrackingMore) and updates order statuses.

## Quick start

### Option 1 — GitHub Codespaces (recommended, no local setup)

Click **Code → Open with Codespaces** in GitHub. The devcontainer starts automatically with the `inmemory` Spring profile — no Docker or Postgres needed.

### Option 2 — Docker Compose (local)

```bash
docker-compose up -d postgres
./gradlew bootRun
```

### Option 3 — In-memory H2 (no Docker)

```bash
./gradlew bootRun --args='--spring.profiles.active=inmemory'
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:webhooks`).

## Healthcheck

```bash
curl localhost:8080/actuator/health
```

## Test a webhook

```bash
curl -X POST localhost:8080/webhooks/stripe \
  -H "Content-Type: application/json" \
  -H "X-Stripe-Signature: test" \
  -d '{"id":"evt_test_001","order_id":"ord_42","status":"SHIPPED"}'
```

## Run tests

```bash
./gradlew test
```

## Structure

- `webhook/` — endpoint + signature filters + service
- `order/` — order domain
- `retry/` — retry scheduler + batch replay
- `carrier/` — partner API client
- `metrics/` — application metrics

## TODO
- Observability (ops dashboard)
- Migrate RestTemplate → WebClient
