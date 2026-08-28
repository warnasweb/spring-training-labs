package com.warnasweb.training.books.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("catalog")
public record CatalogProperties(@NotBlank String name, @Min(1) @Max(100) int maxResults) {}
