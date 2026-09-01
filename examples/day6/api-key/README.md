# Custom API Key Filter

`ApiKeyFilter` reads `X-API-Key`, compares it with a configured value and creates an authenticated principal with `ROLE_API_CLIENT`.

```bash
TRAINING_API_KEY=my-local-key mvn spring-boot:run
curl -i http://localhost:8085/api/books
curl -H 'X-API-Key: my-local-key' http://localhost:8085/api/books
```

This is useful for understanding custom filters, but a single static key has weak identity, rotation and revocation properties. Never put production keys in source control or URLs. Prefer managed credentials, OAuth2 client credentials or mTLS when those requirements apply.
