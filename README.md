# Spring Training Labs

Runnable examples for Spring Boot training. Both applications use the same Book Catalog REST API, making the Day 3 to Day 4 persistence change explicit.

| Example | Storage | Focus |
|---|---|---|
| [`examples/day3`](examples/day3/README.md) | In-memory map; no database dependencies | Spring and Spring Boot annotations, configuration, validation, web, lifecycle, async and scheduling |
| [`examples/day4`](examples/day4/README.md) | H2 by default; PostgreSQL profile available | JPA/Hibernate, repositories, transactions, Flyway and database profiles |

## Requirements

- Java 21
- Maven 3.6.3+

Verify everything from the repository root:

```bash
mvn clean verify
```

Each example is an independent Spring Boot application. See its README for run commands and API examples.
