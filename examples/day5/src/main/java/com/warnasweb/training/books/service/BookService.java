package com.warnasweb.training.books.service;

import com.warnasweb.training.books.domain.Book;
import com.warnasweb.training.books.domain.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import com.warnasweb.training.books.observability.Audited;
import com.warnasweb.training.books.observability.TimedOperation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository books;
    public BookService(BookRepository books) { this.books = books; }
    public List<Book> findAll(String title) { return title == null || title.isBlank() ? books.findAll(Sort.by("id")) : books.findByTitleContainingIgnoreCaseOrderById(title); }
    @TimedOperation
    @Cacheable(cacheNames = "books", key = "#id", sync = true)
    public Book find(long id) { return books.findById(id).orElseThrow(() -> new BookNotFoundException(id)); }
    @Transactional
    @TimedOperation
    @Audited("BOOK_CREATED")
    @CachePut(cacheNames = "books", key = "#result.id")
    public Book create(String isbn, String title, String author, BigDecimal price) {
        if (books.existsByIsbn(isbn)) throw new DuplicateIsbnException(isbn);
        return books.save(new Book(isbn, title, author, price));
    }
    @Transactional
    @TimedOperation
    @Audited("BOOK_UPDATED")
    @CachePut(cacheNames = "books", key = "#result.id")
    public Book update(long id, String isbn, String title, String author, BigDecimal price) {
        var book = books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        if (books.existsByIsbnAndIdNot(isbn, id)) throw new DuplicateIsbnException(isbn);
        book.revise(isbn, title, author, price); return book;
    }
    @Transactional
    @TimedOperation
    @Audited("BOOK_DELETED")
    @CacheEvict(cacheNames = "books", key = "#id")
    public void delete(long id) {
        var book = books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        books.delete(book);
    }
}
