package com.warnasweb.training.security.apikey;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ApiKeyApplication { public static void main(String[] args) { SpringApplication.run(ApiKeyApplication.class, args); } }

record Book(long id, String title) {}
@RestController class BookController {
  @GetMapping("/public") String publicEndpoint() { return "public"; }
  @GetMapping("/api/books") List<Book> books() { return List.of(new Book(1, "Effective Java")); }
}

@Component
class ApiKeyFilter extends OncePerRequestFilter {
  private final String expectedKey;
  ApiKeyFilter(@Value("${training.api-key}") String expectedKey) { this.expectedKey = expectedKey; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String supplied = request.getHeader("X-API-Key");
    if (supplied != null && java.security.MessageDigest.isEqual(supplied.getBytes(), expectedKey.getBytes())) {
      var auth = UsernamePasswordAuthenticationToken.authenticated("training-client", supplied, List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}

@Configuration
class SecurityConfig {
  @Bean SecurityFilterChain security(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
    return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.requestMatchers("/public").permitAll().requestMatchers("/api/**").hasRole("API_CLIENT").anyRequest().denyAll())
        .addFilterBefore(apiKeyFilter, AnonymousAuthenticationFilter.class).build();
  }
}
