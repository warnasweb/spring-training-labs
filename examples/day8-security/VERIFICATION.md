# Day 8 Security Verification

Generated and checked on 2026-09-06.

## Completed Checks

- `mvn -B -Dmaven.repo.local=/private/tmp/day7-m2 clean verify` from `examples/day8-security`
  - Result: PASS
  - Modules: common, api-gateway, product-service, inventory-service, order-service, payment-service, notification-service, discovery-server

- `docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery up --build -d`
  - Result: PASS

- `python3 scripts/smoke.py`
  - Result: PASS
  - Output: `PASS: CRUD, success saga, compensation, idempotency, stock rejection, notifications`

- Manual gateway security check
  - `/products` without bearer token returned `401`
  - `/products` with bearer token returned `200` and the seeded product

## Notes

The example uses a simple signed token and an internal service token for teaching. Keep it as a stepping stone before introducing a real authorization server such as Keycloak, Auth0, Okta, Cognito, or Spring Authorization Server.
