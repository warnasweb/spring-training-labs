# Spring Training Labs

Runnable examples for Spring Boot training. The applications use the same Book Catalog REST API, making each day's evolution explicit.

| Example | Storage | Focus |
|---|---|---|
| [`examples/day3`](examples/day3/README.md) | In-memory map; no database dependencies | Spring and Spring Boot annotations, configuration, validation, web, lifecycle, async and scheduling |
| [`examples/day4`](examples/day4/README.md) | H2 by default; PostgreSQL profile available | JPA/Hibernate, repositories, transactions, Flyway and database profiles |
| [`examples/day5`](examples/day5/README.md) | Day 4 database plus Caffeine cache | Spring AOP, advice and pointcuts, caching, cache coherence and proxy caveats |
| [`examples/day6`](examples/day6/README.md) | Independent Book API security examples | HTTP Basic, form login, JDBC users, JWT, API keys and method security |

## Requirements

- Java 21
- Maven 3.6.3+

Verify everything from the repository root:

```bash
mvn clean verify
```

Each example is an independent Spring Boot application. See its README for run commands and API examples.
