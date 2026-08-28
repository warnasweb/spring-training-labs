# Day 4 — JPA/Hibernate with H2 and PostgreSQL

This example keeps Day 3's Book Catalog API and replaces the in-memory repository with Spring Data JPA. Transactions belong to the service layer, Hibernate validates the Flyway-managed schema, and Open Session in View is disabled.

## H2 (default)

No database installation is needed. The in-memory database is recreated on each run.

```bash
cd examples/day4
mvn clean verify
mvn spring-boot:run
```

API: `http://localhost:8080/api/books`; H2 console: `http://localhost:8080/h2-console`.

H2 console settings: JDBC URL `jdbc:h2:mem:books`, user `sa`, blank password.

## PostgreSQL profile

Create a database, or start any local PostgreSQL instance, then set credentials and activate the profile:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/books
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

The defaults above can be used without environment variables. Flyway creates the schema in both databases. Do not commit real credentials.

## API examples

```bash
curl http://localhost:8080/api/books
curl 'http://localhost:8080/api/books?title=java'
curl -i -X POST http://localhost:8080/api/books \
  -H 'Content-Type: application/json' \
  -d '{"isbn":"9781617297571","title":"Spring in Action","author":"Craig Walls","price":54.99}'
```

Key annotations include `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@Version`, `@Transactional`, and Spring Data's repository abstraction.

## What changed from Day 3

| Layer | Day 3 | Day 4 |
|---|---|---|
| Controller/API | In-memory domain returned directly | Same routes with response DTO mapping |
| Service | Plain service methods | Read-only default plus explicit write transactions |
| Repository | Hand-written map implementation | Spring Data `JpaRepository` with derived queries |
| Domain | Immutable record | Managed JPA entity with generated identity and optimistic version |
| Schema | None | Versioned Flyway migration, validated by Hibernate |

## Suggested walkthrough

1. Run with the default profile and inspect the Flyway and Hibernate startup logs.
2. Follow a create request through the transactional service into the generated repository implementation.
3. Use the H2 console to inspect `books` and `flyway_schema_history`.
4. Explain why `ddl-auto=validate` catches drift and why `open-in-view=false` keeps persistence work in the service boundary.
5. Switch to the PostgreSQL profile without changing Java code.
