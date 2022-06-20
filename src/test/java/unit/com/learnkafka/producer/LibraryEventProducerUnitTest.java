package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import static com.learnkafka.test.repository.BookForTestRepository.kafkaSpringBootDilip;
import static com.learnkafka.test.repository.LibraryEventForTestRepository.libraryEventWithNullId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerUnitTest {

    @Mock
    private KafkaTemplate<Integer, String> mockedKafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LibraryEventProducer underTest;

    @Test
    void test_sendLibraryEventUsingProducerRecord_returnsFailure() throws JsonProcessingException {
        SettableListenableFuture<SendResult<Integer, String>> mockedFuture = new SettableListenableFuture<>();
        // future throws an exception, so onFailure() should be called
        mockedFuture.setException(new RuntimeException("Exception calling Kafka"));
        when(mockedKafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(mockedFuture);

        assertThrows(Exception.class,
                () -> underTest.sendLibraryEventUsingProducerRecord(libraryEventWithNullId(kafkaSpringBootDilip())).get());
    }
}