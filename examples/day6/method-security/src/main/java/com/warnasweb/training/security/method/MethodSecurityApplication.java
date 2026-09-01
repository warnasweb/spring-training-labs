package com.warnasweb.training.security.method;

import java.util.List;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication @EnableMethodSecurity
public class MethodSecurityApplication { public static void main(String[] args) { SpringApplication.run(MethodSecurityApplication.class, args); } }

record Book(long id, String title) {}
@Service class BookService {
  @PreAuthorize("hasAnyRole('READER','ADMIN')") List<Book> findAll() { return List.of(new Book(1, "Effective Java")); }
  @PreAuthorize("hasRole('ADMIN')") Book create(Book book) { return book; }
  @PreAuthorize("#username == authentication.name or hasRole('ADMIN')") String profile(String username) { return "profile:" + username; }
}
@RestController class BookController {
  private final BookService service;
  BookController(BookService service) { this.service = service; }
  @GetMapping("/public") String publicEndpoint() { return "public"; }
  @GetMapping("/api/books") List<Book> books() { return service.findAll(); }
  @PostMapping("/api/books") Book create(@RequestBody Book book) { return service.create(book); }
  @GetMapping("/api/profiles/{username}") String profile(@PathVariable String username) { return service.profile(username); }
}
@Configuration class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
  @Bean UserDetailsService users(PasswordEncoder encoder) { return new InMemoryUserDetailsManager(
      User.withUsername("reader").password(encoder.encode("reader-pass")).roles("READER").build(),
      User.withUsername("admin").password(encoder.encode("admin-pass")).roles("ADMIN").build()); }
  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception { return http.csrf(c -> c.disable()).authorizeHttpRequests(a -> a.requestMatchers("/public").permitAll().anyRequest().authenticated()).httpBasic(Customizer.withDefaults()).build(); }
}
