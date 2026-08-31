package com.warnasweb.training.books.service;

import com.warnasweb.training.books.config.CatalogProperties;
import com.warnasweb.training.books.domain.Book;
import com.warnasweb.training.books.domain.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookService {
	private final BookRepository books;
	private final CatalogProperties properties;

	public BookService(BookRepository books, CatalogProperties properties) {
		this.books = books;
		this.properties = properties;
	}

	public List<Book> findAll(String title) {
		var stream = books.findAll().stream();
		if (title != null && !title.isBlank())
			stream = stream.filter(b -> b.title().toLowerCase().contains(title.toLowerCase()));
		return stream.limit(properties.maxResults()).toList();
	}

	public Book find(long id) {
		return books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
	}

	public Book create(String isbn, String title, String author, BigDecimal price) {
		return save(0, isbn, title, author, price);
	}

	public Book update(long id, String isbn, String title, String author, BigDecimal price) {
		find(id);
		return save(id, isbn, title, author, price);
	}

	public void delete(long id) {
		find(id);
		books.deleteById(id);
	}

	private Book save(long id, String isbn, String title, String author, BigDecimal price) {
		if (books.existsByIsbn(isbn, id))
			throw new DuplicateIsbnException(isbn);
		return books.save(new Book(id, isbn, title, author, price));
	}
}
