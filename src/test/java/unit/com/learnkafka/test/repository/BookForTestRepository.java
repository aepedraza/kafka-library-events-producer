package com.learnkafka.test.repository;

import com.learnkafka.domain.Book;

public final class BookForTestRepository {

    private BookForTestRepository() {
    }

    public static Book kafkaSpringBootDilip() {
        return Book.builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring Boot")
                .build();
    }
}
