package com.warnasweb.training.books.domain;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class BookRepositoryTest {
    @Autowired BookRepository books;
    @Test void persistsAndFindsByTitleIgnoringCase() {
        books.saveAndFlush(new Book("9781617297571", "Spring in Action", "Craig Walls", new BigDecimal("54.99")));
        assertThat(books.findByTitleContainingIgnoreCaseOrderById("SPRING")).singleElement().extracting(Book::getAuthor).isEqualTo("Craig Walls");
    }
}
