package com.warnasweb.training.books.web;

import com.warnasweb.training.books.domain.Book;
import java.math.BigDecimal;

public record BookResponse(long id, String isbn, String title, String author, BigDecimal price, long version) {
	static BookResponse from(Book b) {
		return new BookResponse(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getPrice(), b.getVersion());
	}
}
