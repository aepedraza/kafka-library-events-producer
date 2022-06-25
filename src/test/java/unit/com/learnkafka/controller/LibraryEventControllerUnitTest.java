package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.learnkafka.test.repository.BookForTestRepository.customBook;
import static com.learnkafka.test.repository.BookForTestRepository.kafkaSpringBootDilip;
import static com.learnkafka.test.repository.LibraryEventForTestRepository.libraryEventWithId;
import static com.learnkafka.test.repository.LibraryEventForTestRepository.libraryEventWithNullId;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class) // Unit test for controller layer
@AutoConfigureMockMvc
class LibraryEventControllerUnitTest {

    private static final String EXPECTED_MESSAGE_BOOK_NULL = "book - must not be null";
    private static final String EXPECTED_MESSAGE_BOOK_WITH_NULL_FIELDS = "book.bookAuthor - must not be blank, book.bookId - must not be null";
    private static final String EXPECTED_MESSAGE_LIBRARY_EVENT_ID_NULL = "libraryEventId cannot be null";

    @Autowired
    private MockMvc mockMvc; // access to all endpoints part of controller

    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private LibraryEventProducer producer;

    @Test
    void test_postLibraryEvent_returnsCreated() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithNullId(kafkaSpringBootDilip());
        String json = mapper.writeValueAsString(libraryEvent);

        whenProducerSendsLibraryEventReturnNull(libraryEvent);

        postLibraryEventWithJsonContent(json).andExpect(status().isCreated());
    }

    private void whenProducerSendsLibraryEventReturnNull(LibraryEvent libraryEvent) throws JsonProcessingException {
        when(producer.sendLibraryEventUsingProducerRecord(libraryEvent)).thenReturn(null);
    }

    private ResultActions postLibraryEventWithJsonContent(String json) throws Exception {
        return mockMvc.perform(post("/v1/library-event")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_postLibraryEvent_returns4xx_whenBookIsNull() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithNullId(null);
        String json = mapper.writeValueAsString(libraryEvent);

        whenProducerSendsLibraryEventReturnNull(libraryEvent);

        performPostAndExpect4xxWithErrorMessage(json, EXPECTED_MESSAGE_BOOK_NULL);
    }

    private void performPostAndExpect4xxWithErrorMessage(String json, String expectedMessage) throws Exception {
        postLibraryEventWithJsonContent(json)
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedMessage));
    }

    @Test
    void test_postLibraryEvent_returns4xx_whenBookHasNullFields() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithNullId(customBook(null, null, "The book name"));
        String json = mapper.writeValueAsString(libraryEvent);

        whenProducerSendsLibraryEventReturnNull(libraryEvent);

        performPostAndExpect4xxWithErrorMessage(json, EXPECTED_MESSAGE_BOOK_WITH_NULL_FIELDS);
    }

    @Test
    public void updateLibraryEvent() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithId(kafkaSpringBootDilip());
        String json = mapper.writeValueAsString(libraryEvent);

        whenProducerSendsLibraryEventReturnNull(libraryEvent);

        putLibraryEventWithJsonContent(json).andExpect(status().isOk());
    }

    private ResultActions putLibraryEventWithJsonContent(String json) throws Exception {
        return mockMvc.perform(put("/v1/library-event")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void updateLibraryEvent_withNullLibraryEventId() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithNullId(kafkaSpringBootDilip());
        String json = mapper.writeValueAsString(libraryEvent);

        whenProducerSendsLibraryEventReturnNull(libraryEvent);

        performPutAndExpectBadRequestWithErrorMessage(json, EXPECTED_MESSAGE_LIBRARY_EVENT_ID_NULL);
    }

    private void performPutAndExpectBadRequestWithErrorMessage(String json, String expectedMessage) throws Exception {
        putLibraryEventWithJsonContent(json)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedMessage));
    }
}