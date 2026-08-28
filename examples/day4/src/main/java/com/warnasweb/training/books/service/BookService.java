package com.warnasweb.training.books.service;

import com.warnasweb.training.books.domain.Book;
import com.warnasweb.training.books.domain.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository books;
    public BookService(BookRepository books) { this.books = books; }
    public List<Book> findAll(String title) { return title == null || title.isBlank() ? books.findAll(Sort.by("id")) : books.findByTitleContainingIgnoreCaseOrderById(title); }
    public Book find(long id) { return books.findById(id).orElseThrow(() -> new BookNotFoundException(id)); }
    @Transactional public Book create(String isbn, String title, String author, BigDecimal price) {
        if (books.existsByIsbn(isbn)) throw new DuplicateIsbnException(isbn);
        return books.save(new Book(isbn, title, author, price));
    }
    @Transactional public Book update(long id, String isbn, String title, String author, BigDecimal price) {
        var book = find(id);
        if (books.existsByIsbnAndIdNot(isbn, id)) throw new DuplicateIsbnException(isbn);
        book.revise(isbn, title, author, price); return book;
    }
    @Transactional public void delete(long id) { books.delete(find(id)); }
}
