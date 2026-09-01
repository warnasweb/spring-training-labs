# HTTP Basic

The client sends `Authorization: Basic ...` on every request. The application is stateless and uses two in-memory users.

```bash
mvn spring-boot:run
curl http://localhost:8081/public
curl -u reader:reader-pass http://localhost:8081/api/books
curl -i -u reader:reader-pass -X POST http://localhost:8081/api/books -H 'Content-Type: application/json' -d '{"id":2,"title":"DDD"}'
curl -u admin:admin-pass -X POST http://localhost:8081/api/books -H 'Content-Type: application/json' -d '{"id":2,"title":"DDD"}'
```

Expected: anonymous API call `401`, READER write `403`, ADMIN write `200`. Basic credentials are only encoded, so HTTPS is mandatory outside local training.
