package com.warnasweb.training.books;

import com.warnasweb.training.books.config.CatalogProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(CatalogProperties.class)
@SpringBootApplication
public class Day3Application {
    public static void main(String[] args) { SpringApplication.run(Day3Application.class, args); }
}
