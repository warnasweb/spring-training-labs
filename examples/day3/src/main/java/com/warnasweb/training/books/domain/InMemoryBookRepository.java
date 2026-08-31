package com.warnasweb.training.books.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookRepository implements BookRepository {
	private static final Logger log = LoggerFactory.getLogger(InMemoryBookRepository.class);
	private final AtomicLong ids = new AtomicLong();
	private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();

	@PostConstruct
	void seed() {
		save(new Book(
	            0,
	            "9780134685991",
	            "Effective Java",
	            "Joshua Bloch",
	            new BigDecimal("49.90")));

	    save(new Book(
	            0,
	            "9781617297571",
	            "Spring in Action",
	            "Craig Walls",
	            new BigDecimal("54.99")));

	    save(new Book(
	            0,
	            "9781492072508",
	            "Java in a Nutshell",
	            "Ben Evans",
	            new BigDecimal("44.99")));

	    save(new Book(
	            0,
	            "9780132350884",
	            "Clean Code",
	            "Robert C. Martin",
	            new BigDecimal("42.50")));

	    save(new Book(
	            0,
	            "9780134494166",
	            "Clean Architecture",
	            "Robert C. Martin",
	            new BigDecimal("39.99")));

	    save(new Book(
	            0,
	            "9781617294945",
	            "Spring Boot in Action",
	            "Craig Walls",
	            new BigDecimal("46.99")));
	}

	@PreDestroy
	void clear() {
		log.info("Discarding {} in-memory books", books.size());
		books.clear();
	}

	public List<Book> findAll() {
		return books.values().stream().sorted(Comparator.comparingLong(Book::id)).toList();
	}

	public Optional<Book> findById(long id) {
		return Optional.ofNullable(books.get(id));
	}

	public boolean existsByIsbn(String isbn, long excludedId) {
		return books.values().stream().anyMatch(b -> b.id() != excludedId && b.isbn().equals(isbn));
	}

	public Book save(Book book) {
		long id = book.id() == 0 ? ids.incrementAndGet() : book.id();
		var saved = new Book(id, book.isbn(), book.title(), book.author(), book.price());
		books.put(id, saved);
		return saved;
	}

	public void deleteById(long id) {
		books.remove(id);
	}
}
