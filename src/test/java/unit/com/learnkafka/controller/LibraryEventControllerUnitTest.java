package com.learnkafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.learnkafka.test.repository.BookForTestRepository.kafkaSpringBootDilip;
import static com.learnkafka.test.repository.LibraryEventForTestRepository.libraryEventWithNullId;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class) // Unit test for controller layer
@AutoConfigureMockMvc
class LibraryEventControllerUnitTest {

    @Autowired
    private MockMvc mockMvc; // access to all endpoints part of controller

    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private LibraryEventProducer producer;

    @Test
    void test_postLibraryEvent_returnsCreated() throws Exception {
        LibraryEvent libraryEvent = libraryEventWithNullId(kafkaSpringBootDilip());
        String json = mapper.writeValueAsString(libraryEvent);

        doNothing().when(producer).sendLibraryEventUsingProducerRecord(libraryEvent);

        mockMvc.perform(post("/v1/library-event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}