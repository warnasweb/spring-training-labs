package com.warnasweb.training.books.config;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }

    @Bean @Primary Greeting defaultGreeting(@Value("${catalog.name}") String name) {
        return () -> "Welcome to " + name;
    }

    @Bean @Profile("training") @Qualifier("trainingGreeting")
    Greeting trainingGreeting() { return () -> "Welcome, Spring Boot trainees"; }

    public interface Greeting { String message(); }
}
