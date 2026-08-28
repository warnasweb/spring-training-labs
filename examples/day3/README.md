# Day 3 — Spring annotations, no database

This application deliberately has no JDBC, JPA, H2, or PostgreSQL dependency. A thread-safe in-memory repository is seeded at startup and cleared when the process stops.

## Run and test

```bash
cd examples/day3
mvn clean verify
mvn spring-boot:run
```

API: `http://localhost:8080/api/books`; health: `http://localhost:8080/actuator/health`.

```bash
curl http://localhost:8080/api/books
curl 'http://localhost:8080/api/books?title=spring'
curl -i -X POST http://localhost:8080/api/books \
  -H 'Content-Type: application/json' \
  -d '{"isbn":"9781617297571","title":"Spring in Action","author":"Craig Walls","price":54.99}'
```

Activate the training-specific `@Profile` bean with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=training
```

## Annotation map

- Boot/configuration: `@SpringBootApplication`, `@Configuration`, `@Bean`, `@ConfigurationProperties`, `@Value`, `@Profile`, `@Primary`, `@Qualifier`
- Components: `@Component`, `@Service`, `@Repository`, constructor injection
- Web: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@ResponseStatus`
- Validation/errors: `@Valid`, Jakarta constraints, `@RestControllerAdvice`, `@ExceptionHandler`
- Lifecycle/background: `@PostConstruct`, `@PreDestroy`, `@EnableScheduling`, `@Scheduled`, `@EnableAsync`, `@Async`

Here `@Repository` marks a logical data-access boundary; it does not mean a database is present.

## Suggested walkthrough

1. Start at `Day3Application` to discuss composed Boot configuration annotations.
2. Follow `BookController` → `BookService` → `BookRepository` to show stereotypes and constructor injection.
3. Change `catalog.max-results` to demonstrate type-safe external configuration.
4. Send an invalid request and inspect the validation problem response.
5. Compare the default and `training` profile greetings, then inspect lifecycle, scheduled, and asynchronous logs.

All state is process-local. Restarting the application restores only the seeded book.

## Import the Requestly collection

1. Start the Day 3 application.
2. Open Requestly Desktop and its API Client.
3. Select **Import → Postman**.
4. Upload [`requestly-collection.json`](requestly-collection.json).
5. Open the imported **Spring Training - Day 3 In-Memory API** collection.

The collection uses `baseUrl=http://localhost:8080` and `bookId=1`. Both values can be edited in the collection variables. Run write requests individually because deleting or changing book `1` affects later requests until the application restarts.
