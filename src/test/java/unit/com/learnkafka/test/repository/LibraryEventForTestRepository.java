package com.learnkafka.test.repository;

import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;

public final class LibraryEventForTestRepository {

    private LibraryEventForTestRepository() {
    }

    public static LibraryEvent libraryEventWithNullId(Book book) {
        return LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();
    }

    public static LibraryEvent libraryEventWithId(Book book) {
        return LibraryEvent.builder()
                .libraryEventId(123)
                .book(book)
                .build();
    }
}
