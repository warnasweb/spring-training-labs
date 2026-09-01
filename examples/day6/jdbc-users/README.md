# Database-backed Users

This example keeps HTTP Basic but replaces in-memory users with Spring Security's `JdbcUserDetailsManager`. H2 contains the standard `users` and `authorities` tables; passwords are inserted through a `PasswordEncoder`.

```bash
mvn spring-boot:run
curl -u reader:reader-pass http://localhost:8083/api/books
```

Open `http://localhost:8083/h2-console` with URL `jdbc:h2:mem:securitydb`, user `sa`, and a blank password to inspect the encoded password and authorities. Production systems should use migrations and controlled user-provisioning flows rather than startup insertion.
