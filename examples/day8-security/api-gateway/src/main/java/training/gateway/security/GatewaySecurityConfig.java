package training.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
  @Bean
  SecurityWebFilterChain gatewaySecurity(ServerHttpSecurity http, WebFilter gatewayJwtFilter) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(auth -> auth
            .pathMatchers("/auth/token", "/actuator/health/**", "/actuator/prometheus").permitAll()
            .anyExchange().authenticated())
        .addFilterAt(gatewayJwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }
  @Bean
  WebFilter gatewayJwtFilter(@Value("${security.jwt.secret}") String secret) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getPath().value();
      if (path.equals("/auth/token") || path.startsWith("/actuator/health") || path.equals("/actuator/prometheus")) return chain.filter(exchange);
      String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (header == null || !header.startsWith("Bearer ")) return unauthorized(exchange);
      try {
        var claims = JwtSupport.verify(header.substring(7), secret);
        var auth = new UsernamePasswordAuthenticationToken(claims.subject(), "n/a", claims.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
        return chain.filter(exchange).contextWrite(org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication(auth));
      } catch (IllegalArgumentException ex) { return unauthorized(exchange); }
    };
  }
  private static Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
