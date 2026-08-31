package com.warnasweb.training.books.web;

import com.warnasweb.training.books.config.AppConfiguration.Greeting;
import com.warnasweb.training.books.domain.Book;
import com.warnasweb.training.books.service.BookService;
import com.warnasweb.training.books.service.CatalogTasks;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
	private final BookService service;
	private final CatalogTasks tasks;
	private final Greeting greeting;

	public BookController(BookService service, CatalogTasks tasks, Greeting greeting) {
		this.service = service;
		this.tasks = tasks;
		this.greeting = greeting;
	}

	@GetMapping
	public List<Book> all(@RequestParam(required = false) String title) {
		System.out.println("Method ALL is called.....");
		return service.findAll(title);
	}

	@GetMapping("/{id}")
	public Book one(@PathVariable long id) {
		System.out.println("Method One is called.....");
		return service.find(id);
	}

	@GetMapping("/welcome")
	public Map<String, String> welcome() {
		return Map.of("message", greeting.message());
	}

	@PostMapping
	public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
		var book = service.create(request.isbn(), request.title(), request.author(), request.price());
		tasks.bookCreated(book.id());
		return ResponseEntity.created(URI.create("/api/books/" + book.id())).body(book);
	}

	@PutMapping("/{id}")
	public Book update(@PathVariable long id, @Valid @RequestBody BookRequest r) {
		return service.update(id, r.isbn(), r.title(), r.author(), r.price());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
