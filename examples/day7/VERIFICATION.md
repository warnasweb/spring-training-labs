# Day 7 Verification

Generated and checked on 2026-09-06.

## Completed checks

- `mvn -B -Dmaven.repo.local=/private/tmp/day7-m2 clean verify -Plegacy-discovery` from `outputs/day7-microservices`
  - Result: PASS
  - Modules: common, api-gateway, product-service, inventory-service, order-service, payment-service, notification-service, optional discovery-server
  - Coverage examples: outbox publisher, gateway routes, product validation, inventory reservation concurrency, order saga worker, payment idempotency and rollback tests

- Earlier full repository verification from `work/spring-training-labs`
  - Result: PASS before the final Day 7 gateway package/route bean-name fix was synced into `examples/day7`
  - Existing Days 3-6 modules were included in that repository-level run

## Runtime check status

Docker Compose database and Kafka startup were corrected during verification, including Kafka KRaft storage path handling. The business services and gateway reached healthy state after the Spring Boot / Spring Cloud / Resilience4j dependency alignment fix.

- `docker compose --profile apps up --build -d api-gateway`
  - Result: PASS
  - Gateway image rebuilt and container started successfully.

- `python3 scripts/smoke.py`
  - Result: PASS
  - Output: `PASS: CRUD, success saga, compensation, idempotency, stock rejection, notifications`

## Recommended final runtime check

```bash
cd examples/day7
cp .env.example .env
docker compose --profile apps up --build -d
python3 scripts/smoke.py
docker compose --profile apps down -v
```

Expected result: the smoke script creates products/orders through the gateway, confirms one order, cancels one failed-payment order with stock compensation, rejects an insufficient-stock order, verifies idempotency behavior, and prints `PASS`.
