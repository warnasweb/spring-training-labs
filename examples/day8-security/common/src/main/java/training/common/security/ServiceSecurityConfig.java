package training.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServiceSecurityConfig {
  @Bean
  SecurityFilterChain serviceSecurity(HttpSecurity http, ServiceJwtFilter jwt) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
  @Bean
  ServiceJwtFilter serviceJwtFilter(@Value("${security.jwt.secret}") String secret, @Value("${security.internal-token}") String internalToken) { return new ServiceJwtFilter(secret, internalToken); }
  static class ServiceJwtFilter extends OncePerRequestFilter {
    private final String secret;
    private final String internalToken;
    ServiceJwtFilter(String secret, String internalToken) { this.secret = secret; this.internalToken = internalToken; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
      String path = req.getRequestURI();
      if (path.startsWith("/actuator/health") || path.equals("/actuator/prometheus")) { chain.doFilter(req, res); return; }
      String internal = req.getHeader("X-Internal-Token");
      if (internalToken.equals(internal)) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("internal-service", "n/a", java.util.List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))));
        chain.doFilter(req, res);
        return;
      }
      String header = req.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) { res.sendError(401); return; }
      try {
        var claims = JwtSupport.verify(header.substring(7), secret);
        var authorities = claims.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(claims.subject(), "n/a", authorities));
        chain.doFilter(req, res);
      } catch (IllegalArgumentException ex) { res.sendError(401); }
    }
  }
}
