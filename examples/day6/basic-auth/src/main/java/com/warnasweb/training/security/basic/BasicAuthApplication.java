package com.warnasweb.training.security.basic;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class BasicAuthApplication {
  public static void main(String[] args) { SpringApplication.run(BasicAuthApplication.class, args); }
}

record Book(long id, String title) {}

@RestController
class BookController {
  @GetMapping("/public") String publicEndpoint() { return "public"; }
  @GetMapping("/api/books") List<Book> books() { return List.of(new Book(1, "Effective Java")); }
  @PostMapping("/api/books") Book create(@RequestBody Book book) { return book; }
}

@Configuration
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

  @Bean UserDetailsService users(PasswordEncoder encoder) {
    return new InMemoryUserDetailsManager(
        User.withUsername("reader").password(encoder.encode("reader-pass")).roles("READER").build(),
        User.withUsername("admin").password(encoder.encode("admin-pass")).roles("ADMIN").build());
  }

  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("READER", "ADMIN")
            .requestMatchers("/api/books/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults()).build();
  }
}
