package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static com.learnkafka.test.repository.BookForTestRepository.kafkaSpringBootDilip;
import static com.learnkafka.test.repository.LibraryEventForTestRepository.libraryEventWithNullId;
import static org.assertj.core.api.Assertions.assertThat;
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
    public void test_sendLibraryEventUsingProducerRecord_returnsFailure() {
        SettableListenableFuture<SendResult<Integer, String>> mockedFuture = new SettableListenableFuture<>();
        // future throws an exception, so onFailure() should be called
        mockedFuture.setException(new RuntimeException("Exception calling Kafka"));
        when(mockedKafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(mockedFuture);

        assertThrows(Exception.class,
                () -> underTest.sendLibraryEventUsingProducerRecord(libraryEventWithNullId(kafkaSpringBootDilip())).get());
    }

    @Test
    public void test_sendLibraryEventUsingProducerRecord_returnsSuccess() throws JsonProcessingException, ExecutionException, InterruptedException {
        SettableListenableFuture<SendResult<Integer, String>> mockedFuture = new SettableListenableFuture<>();
        // mockedFuture returns an object, so onSuccess() should be called
        LibraryEvent libraryEvent = libraryEventWithNullId(kafkaSpringBootDilip());
        mockedFuture.set(testSendResult(libraryEvent));

        when(mockedKafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(mockedFuture);

        ListenableFuture<SendResult<Integer, String>> obtainedFuture = underTest.sendLibraryEventUsingProducerRecord(libraryEvent);

        assertThat(obtainedFuture.isDone()).isTrue();

        SendResult<Integer, String> obtainedResult = obtainedFuture.get();
        assertThat(obtainedResult.getProducerRecord().topic()).isEqualTo("library-events");
    }

    private SendResult<Integer, String> testSendResult(LibraryEvent libraryEvent) throws JsonProcessingException {
        String topic = "library-events";
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(topic, key, value);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 1), 1L, 1, 123L, 1, 2);

        return new SendResult<>(producerRecord, recordMetadata);
    }
}