package com.warnasweb.training.books.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BookRequest(@NotBlank @Pattern(regexp = "[0-9-]{10,17}") String isbn,
        @NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 120) String author,
        @NotNull @DecimalMin("0.01") BigDecimal price) {}
