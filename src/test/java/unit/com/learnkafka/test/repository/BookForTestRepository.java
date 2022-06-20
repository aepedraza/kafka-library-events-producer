package com.learnkafka.test.repository;

import com.learnkafka.domain.Book;

public final class BookForTestRepository {

    private BookForTestRepository() {
    }

    public static Book kafkaSpringBootDilip() {
        return customBook(123, "Dilip", "Kafka using Spring Boot");
    }

    public static Book customBook(Integer bookId, String bookAuthor, String bookName) {
        return Book.builder()
                .bookId(bookId)
                .bookAuthor(bookAuthor)
                .bookName(bookName)
                .build();
    }
}
