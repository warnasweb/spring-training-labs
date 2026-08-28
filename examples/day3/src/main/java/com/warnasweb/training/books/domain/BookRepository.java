package com.warnasweb.training.books.domain;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findById(long id);
    boolean existsByIsbn(String isbn, long excludedId);
    Book save(Book book);
    void deleteById(long id);
}
