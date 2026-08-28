package com.warnasweb.training.books.domain;

import java.math.BigDecimal;

public record Book(long id, String isbn, String title, String author, BigDecimal price) {}
