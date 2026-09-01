# JWT Resource Server

The API validates signed bearer tokens and maps the `scope` claim to `SCOPE_...` authorities. `/token` is a lab-only issuer protected with HTTP Basic so the example is runnable without an external identity provider.

```bash
mvn spring-boot:run
TOKEN=$(curl -s -u reader:reader-pass -X POST http://localhost:8084/token | jq -r .token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8084/api/books
```

Use `admin / admin-pass` to receive `books.read books.write`. The shared HMAC secret is intentionally local to this training application. Production resource servers should normally trust an external issuer and public keys, validate audience and issuer, and never embed a production signing secret in source code.
