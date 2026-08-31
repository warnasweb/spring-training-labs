package com.warnasweb.training.books.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.warnasweb.training.books.domain.BookRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class BookCachingTest {
    @Autowired BookService service;
    @Autowired CacheManager cacheManager;
    @MockitoSpyBean BookRepository books;

    @BeforeEach
    void clearCacheAndSpyHistory() {
        cacheManager.getCache("books").clear();
        clearInvocations(books);
    }

    @Test
    void repeatedReadLoadsTheRepositoryOnce() {
        service.find(1L);
        service.find(1L);

        verify(books, times(1)).findById(1L);
    }

    @Test
    void updateRefreshesTheCachedValue() {
        service.find(1L);
        service.update(1L, "9780134685991", "Effective Java, Third Edition", "Joshua Bloch",
                new BigDecimal("51.90"));
        clearInvocations(books);

        assertThat(service.find(1L).getTitle()).isEqualTo("Effective Java, Third Edition");
        verify(books, times(0)).findById(1L);
    }
}
