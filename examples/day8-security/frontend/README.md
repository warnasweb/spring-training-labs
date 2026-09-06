# Day 8 React Frontend

This frontend is a small teaching UI for the secured Day 8 microservices example.

It calls only the API Gateway and demonstrates:

- token login with `POST /auth/token`
- secured product and inventory reads
- order creation
- Saga success and payment-failure compensation
- payment and notification lookups

## Local development

Start the backend first from `examples/day8-security`:

```bash
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery up --build -d
```

Then run the frontend locally:

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

The Vite dev server proxies `/api` to `http://localhost:8180`.

## Docker mode

The main compose file can also run the frontend container:

```bash
docker compose -f docker-compose.yml -f docker-compose.discovery.yml --profile apps --profile discovery --profile frontend up --build -d
```

Open:

```text
http://localhost:5173
```
