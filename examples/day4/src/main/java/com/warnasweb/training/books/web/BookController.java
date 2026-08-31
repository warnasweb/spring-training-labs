package com.warnasweb.training.books.web;

import com.warnasweb.training.books.service.BookService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
	private final BookService service;

	public BookController(BookService service) {
		this.service = service;
	}

	@GetMapping
	public List<BookResponse> all(@RequestParam(required = false) String title) {
		return service.findAll(title).stream().map(BookResponse::from).toList();
	}

	@GetMapping("/{id}")
	public BookResponse one(@PathVariable long id) {
		return BookResponse.from(service.find(id));
	}

	@PostMapping
	public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest r) {
		var book = service.create(r.isbn(), r.title(), r.author(), r.price());
		return ResponseEntity.created(URI.create("/api/books/" + book.getId())).body(BookResponse.from(book));
	}

	@PutMapping("/{id}")
	public BookResponse update(@PathVariable long id, @Valid @RequestBody BookRequest r) {
		return BookResponse.from(service.update(id, r.isbn(), r.title(), r.author(), r.price()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
