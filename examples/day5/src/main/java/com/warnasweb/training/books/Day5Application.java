package com.warnasweb.training.books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Day5Application {
    public static void main(String[] args) { SpringApplication.run(Day5Application.class, args); }
}
