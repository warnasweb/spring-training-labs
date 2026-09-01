# Day 6 — Spring Security Implementations

Day 6 continues the Book Catalog theme with six deliberately separate applications. Each one teaches a security mechanism without hiding the important behavior behind a large combined configuration.

## Examples

| Folder | Port | Authentication | State | Main lesson |
|---|---:|---|---|---|
| `basic-auth` | 8081 | Username/password in the HTTP Basic header | Stateless per request | The smallest API security configuration |
| `form-login` | 8082 | HTML login form | HTTP session | Browser login, session cookie, CSRF and logout |
| `jdbc-users` | 8083 | HTTP Basic with users loaded from H2 | Stateless per request | Database-backed identities and encoded passwords |
| `jwt-resource-server` | 8084 | Signed bearer JWT | Stateless | JWT signature/expiry validation and scope authorization |
| `api-key` | 8085 | Custom `X-API-Key` header | Stateless | Writing and placing a custom authentication filter |
| `method-security` | 8086 | HTTP Basic | Stateless per request | `@EnableMethodSecurity` and `@PreAuthorize` at the service boundary |

Run all tests from this directory:

```bash
mvn clean verify
```

Run one example:

```bash
cd basic-auth
mvn spring-boot:run
```

Every application exposes:

- `GET /api/books` — read the sample catalog
- `POST /api/books` — demonstrate a write permission where applicable
- `GET /public` — an intentionally public endpoint

Each child README contains credentials, commands, expected status codes and focused exercises.

## Why these are separate applications

Combining form login, Basic authentication, JWT, and a custom API key in one filter chain makes the teaching example harder to reason about. Separate applications make the credential source, session policy, filters and failure behavior visible. Production systems may use multiple `SecurityFilterChain` beans, but should do so only when request boundaries and precedence are explicit.

## External identity patterns

OAuth2/OIDC login, OAuth2 Authorization Server, LDAP/Active Directory, SAML 2.0 and mTLS are important production patterns, but need an external identity or certificate environment. They are not represented here by fake implementations. The JWT example models the resource-server side used behind a real identity provider.

## Suggested teaching order

1. Start with `basic-auth` and trace a request through the filter chain.
2. Compare `form-login` to see where the authenticated identity is stored.
3. Replace in-memory identities with `jdbc-users`.
4. Move to stateless bearer tokens with `jwt-resource-server`.
5. Use `api-key` to understand a custom filter—and its operational limitations.
6. Finish with `method-security` so authorization remains effective beyond controllers.
