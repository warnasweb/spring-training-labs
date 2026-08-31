# Day 5 — Spring AOP and Caching

This example continues Day 4's JPA Book Catalog without changing its REST API or database schema. Day 5 adds Spring AOP for performance logging and auditing, plus Spring's cache abstraction backed by Caffeine.

## Run the lab

H2 remains the default, so no database installation is required.

```bash
cd examples/day5
mvn clean verify
mvn spring-boot:run
```

- API: `http://localhost:8080/api/books`
- H2 console: `http://localhost:8080/h2-console`
- Cache actuator: `http://localhost:8080/actuator/caches`
- Metrics actuator: `http://localhost:8080/actuator/metrics`

H2 console settings: JDBC URL `jdbc:h2:mem:books`, user `sa`, blank password. The Day 4 PostgreSQL profile is still available.

## Try the cache

Call the same endpoint twice:

```bash
curl http://localhost:8080/api/books/1
curl http://localhost:8080/api/books/1
```

The first call is a cache miss and executes `BookService.find`. The result is stored under cache `books`, key `1`. The second call returns from Caffeine, so the method body does not run. The timing advice may still run around the cache interceptor; advice ordering determines whether it measures the proxy chain or only target execution.

Update the book and read it again:

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H 'Content-Type: application/json' \
  -d '{"isbn":"9780134685991","title":"Effective Java, Third Edition","author":"Joshua Bloch","price":51.90}'

curl http://localhost:8080/api/books/1
```

`@CachePut` always runs the update method and refreshes key `1` with its result. Delete uses `@CacheEvict` so a removed book cannot remain readable from the cache.

## AOP examples

`PerformanceAspect` demonstrates `@Around` advice selected by the custom `@TimedOperation` annotation. It proceeds exactly once, logs failures without swallowing them, and records duration in a `finally` block.

`AuditAspect` demonstrates annotation-based pointcuts with `@AfterReturning` and `@AfterThrowing`. Write methods declare business intent with `@Audited("BOOK_UPDATED")` rather than relying on fragile method-name pointcuts.

The audit output is deliberately a teaching example. A production audit trail normally needs authenticated actor identity, entity identifiers, timestamps, old/new values, durable storage, access controls, and a policy for sensitive information.

## Caching examples

| Method | Annotation | Behavior |
|---|---|---|
| `find` | `@Cacheable(sync = true)` | A hit skips the method; concurrent misses for a key may be coalesced by the provider |
| `create` | `@CachePut` | Always saves and then populates the new ID |
| `update` | `@CachePut` | Always updates and refreshes the cached ID |
| `delete` | `@CacheEvict` | Evicts the ID after a successful delete |

Caffeine is configured with a maximum of 1,000 entries, a ten-minute expire-after-write policy, and statistics recording. These values are illustrative; production TTL and capacity must follow freshness, traffic, memory, and failure requirements.

## The proxy boundary

Both AOP and annotation-driven caching use Spring proxies. Calls entering `BookService` from `BookController` are intercepted. A call such as `this.find(id)` from another `BookService` method would bypass the proxy, so `@TimedOperation` and `@Cacheable` would not apply to that internal call.

The write methods deliberately query `BookRepository` directly instead of calling the cached `find` method. In larger applications, extract the cached read into a separate Spring bean when one service must call it through a proxy.

## Tests to study

- `BookCachingTest.repeatedReadLoadsTheRepositoryOnce` proves the second read is a cache hit by verifying the repository is called once.
- `BookCachingTest.updateRefreshesTheCachedValue` proves a write refreshes the cache and the next read avoids the repository.
- The cache is cleared before every test to prevent order-dependent results.

## Suggested exercises

1. Add `@Before`, `@After`, `@AfterReturning`, and `@AfterThrowing` advice to a temporary lifecycle aspect and compare the logs.
2. Replace the annotation pointcut with `execution(* com.warnasweb.training.books.service.*.*(..))` and explain every wildcard.
3. Add a title-search cache with a key that includes the normalized title.
4. Create a self-invocation example, observe the missing interception, then refactor the cached method into another bean.
5. Lower Caffeine's TTL and maximum size, then observe expiration and eviction.
6. Discuss what changes when a shared Redis cache replaces a per-instance Caffeine cache.

## What changed from Day 4

| Day 4 | Day 5 |
|---|---|
| Service calls go directly to transactional behavior | Spring proxies also apply timing, audit, and cache interceptors |
| Every `GET /api/books/{id}` reaches JPA | Repeated ID reads can return from Caffeine |
| Writes only modify the database | Writes also refresh or evict affected cache entries |
| No cache lifecycle | Bounded size, TTL, hit/miss behavior and stale-data risks are explicit |
