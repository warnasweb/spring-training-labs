# Day 8 - Secured Spring Boot Microservices

This example keeps the Day 7 e-commerce microservices system intact and adds a training-focused security layer. The original Day 7 code remains in `examples/day7`; this folder is a separate runnable copy for teaching gateway authentication, downstream authorization, service-to-service trust, and how security interacts with Saga, Kafka and database ownership.

## Services

| Service | Host port | Container port | Database port | Purpose |
|---|---:|---:|---:|---|
| api-gateway | 8180 | 8080 | - | Routes requests and enforces bearer token authentication |
| product-service | 8181 | 8081 | 5533 | Product CRUD |
| inventory-service | 8182 | 8082 | 5534 | Stock query, reserve, release |
| order-service | 8183 | 8083 | 5535 | Order workflow, outbox, Saga worker |
| payment-service | 8184 | 8084 | 5536 | Kafka payment consumer |
| notification-service | 8185 | 8085 | 5537 | Kafka notification consumer |
| discovery-server | 8861 | 8761 | - | Eureka dashboard and registration demo |
| frontend | 5173 | 80 | - | React UI that calls only the API Gateway |
| kafka | 9192 | 9092 | - | `shop.events` event stream |

The different host ports allow Day 7 and Day 8 to coexist on the same machine. Inside Docker, services still use the normal 8080-8085 ports.

## Security Model

The lab uses a simple HMAC-signed JWT-style token so security can be demonstrated without adding Keycloak or another identity provider. The gateway exposes `POST /auth/token`, then protects all business routes. Health and Prometheus endpoints remain open for operations demos.

Business services also validate the bearer token when called directly. The `order-service` calls `inventory-service` using `X-Internal-Token` to demonstrate service-to-service authentication for scheduled Saga work, where no user request is active. This is intentionally simple for training; in production you would normally use OAuth2/OIDC, short-lived JWTs, mTLS, or a service mesh.

## Run Everything

```bash
cd /Users/rajesh.warna/Documents/work/microservices/spring-training-labs/examples/day8-security
cp .env.example .env
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery up --build -d
```

Open Eureka:

```text
http://localhost:8861
```

Expected registered applications:

```text
API-GATEWAY
PRODUCT-SERVICE
INVENTORY-SERVICE
ORDER-SERVICE
PAYMENT-SERVICE
NOTIFICATION-SERVICE
```

## Frontend UI

Start the backend plus React UI with Docker:

```bash
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery --profile frontend up --build -d
```

Open:

```text
http://localhost:5173
```

The UI demonstrates login, bearer-token API calls, Product CRUD, inventory stock lookup, direct stock reserve/release, order creation, order cancellation, Saga success, payment failure compensation, and automatic payment/notification refresh. The frontend calls only the API Gateway.

For local frontend development:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to `http://localhost:8180`.

## Test

```bash
python3 scripts/smoke.py
```

Expected output:

```text
PASS: CRUD, success saga, compensation, idempotency, stock rejection, notifications
```

The smoke test obtains a token from `/auth/token`, calls all APIs through the secured gateway, and verifies that the Saga still works with service-to-service security enabled.

## Manual Security Checks

Without a token, business routes are rejected:

```bash
curl -i http://localhost:8180/products
```

Expected status: `401 Unauthorized`.

Get a token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"trainer","password":"training"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])')
```

Call secured APIs:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8180/products
curl -H "Authorization: Bearer $TOKEN" http://localhost:8180/inventory/11111111-1111-1111-1111-111111111111
```

Create an order:

```bash
curl -X POST http://localhost:8180/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: security-order-1" \
  -d '{"productId":"11111111-1111-1111-1111-111111111111","quantity":2,"failPayment":false}'

curl -H "Authorization: Bearer $TOKEN" http://localhost:8180/orders/security-order-1
```

## DBeaver Connections

| Service | Host | Port | Database | Username | Password |
|---|---:|---:|---|---|---|
| Product | localhost | 5533 | product | product | training |
| Inventory | localhost | 5534 | inventory | inventory | training |
| Order | localhost | 5535 | order | order | training |
| Payment | localhost | 5536 | payment | payment | training |
| Notification | localhost | 5537 | notification | notification | training |

Useful tables: `products`, `stock`, `reservations`, `orders`, `payments`, `notifications`, `outbox`, `inbox`, and `flyway_schema_history`.

## Stop

```bash
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery --profile frontend down
```

Use `down -v` only when you intentionally want to delete the PostgreSQL and Kafka volumes for this example.

## Teaching Flow

1. Show Day 7 running without security.
2. Start Day 8 and show that `/products` returns 401 without a token.
3. Issue a token from `/auth/token` and retry the same request successfully.
4. Show that direct service endpoints also require authentication.
5. Use the frontend Product CRUD section to show that Product owns product data while Inventory owns stock.
6. Use direct reserve/release to explain idempotent inventory commands.
7. Create a successful order and watch the UI auto-refresh order, payment and notification data.
8. Create a failed-payment order and show Saga compensation releasing stock.
9. Cancel an in-flight order and explain why confirmed-order cancellation would require a refund flow.
10. Explain why `order-service` uses `X-Internal-Token` for the scheduled Saga call to inventory.
11. Open Eureka at `http://localhost:8861` and show all secured services registered.
12. Discuss production replacements: OAuth2/OIDC provider, resource server JWT validation, mTLS, API gateway policy, secret rotation, and method-level authorization.
