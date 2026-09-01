# Method Security

The HTTP chain requires authentication, while `BookService` owns fine-grained authorization through `@PreAuthorize`.

```bash
mvn spring-boot:run
curl -u reader:reader-pass http://localhost:8086/api/books
curl -i -u reader:reader-pass -X POST http://localhost:8086/api/books -H 'Content-Type: application/json' -d '{"id":2,"title":"DDD"}'
curl -u reader:reader-pass http://localhost:8086/api/profiles/reader
```

This protects the service when it is called from controllers, jobs or messaging adapters. It still uses Spring proxies: self-invocation inside the same service can bypass method-security advice.
