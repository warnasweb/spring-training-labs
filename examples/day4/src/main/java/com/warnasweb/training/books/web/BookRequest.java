package com.warnasweb.training.books.web;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BookRequest(@NotBlank @Pattern(regexp = "[0-9-]{10,17}") String isbn,
		@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 120) String author,
		@NotNull @DecimalMin("0.01") BigDecimal price) {
}
