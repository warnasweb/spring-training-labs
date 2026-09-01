package com.warnasweb.training.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class JwtResourceServerApplication { public static void main(String[] args) { SpringApplication.run(JwtResourceServerApplication.class, args); } }

record Book(long id, String title) {}
record TokenResponse(String token, long expiresIn) {}

@RestController
class BookController {
  private final JwtEncoder encoder;
  BookController(JwtEncoder encoder) { this.encoder = encoder; }
  @GetMapping("/public") String publicEndpoint() { return "public"; }
  @GetMapping("/api/books") List<Book> books() { return List.of(new Book(1, "Effective Java")); }
  @PostMapping("/api/books") Book create(@RequestBody Book book) { return book; }
  @PostMapping("/token") TokenResponse token(Authentication authentication) {
    Instant now = Instant.now();
    String scope = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "books.read books.write" : "books.read";
    JwtClaimsSet claims = JwtClaimsSet.builder().issuer("day6-lab").subject(authentication.getName()).issuedAt(now).expiresAt(now.plusSeconds(900)).claim("scope", scope).build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return new TokenResponse(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), 900);
  }
}

@Configuration
class SecurityConfig {
  private static final byte[] SECRET = "day6-training-secret-key-must-be-32-bytes-minimum".getBytes(java.nio.charset.StandardCharsets.UTF_8);
  @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
  @Bean UserDetailsService users(PasswordEncoder encoder) { return new InMemoryUserDetailsManager(
      User.withUsername("reader").password(encoder.encode("reader-pass")).roles("READER").build(),
      User.withUsername("admin").password(encoder.encode("admin-pass")).roles("ADMIN").build()); }
  @Bean SecretKey secretKey() { return new SecretKeySpec(SECRET, "HmacSHA256"); }
  @Bean JwtDecoder decoder(SecretKey key) { return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build(); }
  @Bean JwtEncoder encoder(SecretKey key) {
    OctetSequenceKey jwk = new OctetSequenceKey.Builder(key.getEncoded()).algorithm(JWSAlgorithm.HS256).build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
  }
  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.requestMatchers("/public").permitAll().requestMatchers("/token").hasAnyRole("READER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/books/**").hasAuthority("SCOPE_books.read")
            .requestMatchers("/api/books/**").hasAuthority("SCOPE_books.write").anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults()).oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults())).build();
  }
}
