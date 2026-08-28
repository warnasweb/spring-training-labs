package com.warnasweb.training.books.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogTasks {
    private static final Logger log = LoggerFactory.getLogger(CatalogTasks.class);
    @Scheduled(fixedDelayString = "${catalog.report-delay:60000}") void report() { log.debug("Scheduled catalog check completed"); }
    @Async public void bookCreated(long id) { log.info("Asynchronously observed book creation: {}", id); }
}
