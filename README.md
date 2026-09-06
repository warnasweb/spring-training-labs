# Spring Training Labs

Runnable examples for Spring Boot training. Days 3–6 evolve a Book Catalog REST API; Day 7 introduces a separate e-commerce microservices system; Day 8 repeats it with gateway and service security.

| Example | Storage | Focus |
|---|---|---|
| [`examples/day3`](examples/day3/README.md) | In-memory map; no database dependencies | Spring and Spring Boot annotations, configuration, validation, web, lifecycle, async and scheduling |
| [`examples/day4`](examples/day4/README.md) | H2 by default; PostgreSQL profile available | JPA/Hibernate, repositories, transactions, Flyway and database profiles |
| [`examples/day5`](examples/day5/README.md) | Day 4 database plus Caffeine cache | Spring AOP, advice and pointcuts, caching, cache coherence and proxy caveats |
| [`examples/day6`](examples/day6/README.md) | Independent Book API security examples | HTTP Basic, form login, JDBC users, JWT, API keys and method security |

| [`examples/day7`](examples/day7/README.md) | Five PostgreSQL databases and Kafka | Gateway, REST, resilience, Saga compensation, transactional outbox, idempotency and observability |
| [`examples/day8-security`](examples/day8-security/README.md) | Five PostgreSQL databases and Kafka | Same microservices plus JWT-style gateway security and internal service-token calls |

## Requirements

- Java 21
- Maven 3.9+
- Docker with Compose v2 for Day 7/Day 8 and their PostgreSQL integration tests

Verify everything from the repository root:

```bash
mvn clean verify
```

Each example is an independent Spring Boot application. See its README for run commands and API examples.
