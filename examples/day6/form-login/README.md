# Form Login and Session

Open `http://localhost:8082/books`. Spring redirects the anonymous browser to `/login`. Sign in with `reader / reader-pass`; the authenticated identity is then retained in the HTTP session.

```bash
mvn spring-boot:run
```

Inspect the session cookie, the hidden CSRF token in the logout form, and the behavior after logout. This pattern fits server-rendered browser applications; it is different from a stateless bearer-token API.
