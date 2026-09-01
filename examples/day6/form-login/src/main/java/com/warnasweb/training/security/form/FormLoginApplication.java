package com.warnasweb.training.security.form;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class FormLoginApplication { public static void main(String[] args) { SpringApplication.run(FormLoginApplication.class, args); } }

@Controller
class PagesController {
  @GetMapping("/public") @ResponseBody String publicEndpoint() { return "public"; }
  @GetMapping("/books") String books(Model model) { model.addAttribute("books", List.of("Effective Java", "Domain-Driven Design")); return "books"; }
}

@Configuration
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
  @Bean UserDetailsService users(PasswordEncoder encoder) { return new InMemoryUserDetailsManager(User.withUsername("reader").password(encoder.encode("reader-pass")).roles("READER").build()); }
  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(a -> a.requestMatchers("/public").permitAll().anyRequest().authenticated())
        .formLogin(Customizer.withDefaults()).logout(Customizer.withDefaults()).build();
  }
}
