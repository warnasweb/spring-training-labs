package com.warnasweb.training.security.jdbc;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class JdbcUsersApplication { public static void main(String[] args) { SpringApplication.run(JdbcUsersApplication.class, args); } }

record Book(long id, String title) {}
@RestController class BookController {
  @GetMapping("/public") String publicEndpoint() { return "public"; }
  @GetMapping("/api/books") List<Book> books() { return List.of(new Book(1, "Effective Java")); }
  @PostMapping("/api/books") Book create(@RequestBody Book book) { return book; }
}

@Configuration
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

  @Bean @DependsOnDatabaseInitialization JdbcUserDetailsManager users(DataSource dataSource, PasswordEncoder encoder) {
    JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
    if (!users.userExists("reader")) users.createUser(User.withUsername("reader").password(encoder.encode("reader-pass")).roles("READER").build());
    if (!users.userExists("admin")) users.createUser(User.withUsername("admin").password(encoder.encode("admin-pass")).roles("ADMIN").build());
    return users;
  }

  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(a -> a
        .requestMatchers("/public", "/h2-console/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("READER", "ADMIN")
        .requestMatchers("/api/books/**").hasRole("ADMIN").anyRequest().authenticated())
        .headers(h -> h.frameOptions(f -> f.sameOrigin())).httpBasic(Customizer.withDefaults()).build();
  }
}
