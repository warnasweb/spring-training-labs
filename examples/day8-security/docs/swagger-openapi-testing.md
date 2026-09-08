# Final Day: Swagger/OpenAPI and Testing Examples

This folder supports the final training day topics: API documentation and testing. The examples extend `day8-security` because the secured lab shows the most useful OpenAPI behavior: JWT authorization, protected endpoints, gateway routing, validation errors, and saga endpoints.

## Swagger/OpenAPI demo

Start the secured stack:

```bash
cd /Users/rajesh.warna/Documents/work/microservices/spring-training-labs/examples/day8-security
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery up --build -d
```

Open these pages:

| Component | Swagger UI | OpenAPI JSON |
|---|---|---|
| API Gateway | http://localhost:8180/swagger-ui.html | http://localhost:8180/v3/api-docs |
| Product service | http://localhost:8181/swagger-ui.html | http://localhost:8181/v3/api-docs |
| Inventory service | http://localhost:8182/swagger-ui.html | http://localhost:8182/v3/api-docs |
| Order service | http://localhost:8183/swagger-ui.html | http://localhost:8183/v3/api-docs |

Get a JWT through the gateway:

```bash
curl -s -X POST http://localhost:8180/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"trainer","password":"training"}'
```

In Swagger UI, click **Authorize** and paste:

```text
Bearer <accessToken>
```

Then try Product CRUD, Inventory reservation, Order creation, and Order cancellation.

## What to explain during class

1. `OpenAPI` is the contract. `Swagger UI` is the browser tool that renders it.
2. Business services use `springdoc-openapi-starter-webmvc-ui` through the shared `common` module.
3. The gateway uses `springdoc-openapi-starter-webflux-ui` because Spring Cloud Gateway is WebFlux based.
4. Swagger endpoints are allowed without a token for local training. Business APIs still require JWT or the internal service token.
5. Controller annotations document the behavior that generated documentation cannot infer, such as idempotency keys and saga compensation.

## Testing examples added

Run the focused product-service tests:

```bash
cd /Users/rajesh.warna/Documents/work/microservices/spring-training-labs/examples/day8-security
mvn -pl product-service -am test
```

The test `ProductApiDocumentationAndSecurityTest` demonstrates:

- `/v3/api-docs` is readable without a token.
- the generated contract declares bearer JWT security.
- `/products` rejects a missing token with 401.
- validation still runs after authorization and returns 400 for invalid product input.

Run the existing inventory integration tests:

```bash
mvn -pl inventory-service -am test
```

Those tests use PostgreSQL Testcontainers and demonstrate reserve/release idempotency, insufficient stock, and concurrent oversell protection.

Run all Day 8 tests:

```bash
mvn test
```

If Docker is not running, Testcontainers tests are skipped where configured with `disabledWithoutDocker=true`.
