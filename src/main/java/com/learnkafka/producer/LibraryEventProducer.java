package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final String topic = "library-events";

    /**
     * Sends event asynchronously using Kafka with default topic.
     *
     * @param libraryEvent event to be sent by Kafka
     * @throws JsonProcessingException if event cannot be converted to JSON
     */
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        // Sends to default topic
        // If key is null, send to available partition using round-robin
        ListenableFuture<SendResult<Integer, String>> resultFuture = kafkaTemplate.sendDefault(key, value);

        // Callback executed in a different thread
        resultFuture.addCallback(loggingCallback(key, value));
    }

    private ListenableFutureCallback<SendResult<Integer, String>> loggingCallback(Integer key, String value) {
        return new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error sending message. Key: {}, value: {}", key, value);
                try {
                    throw ex;
                } catch (Throwable e) {
                    log.error("Error in onFailure: {}", e.getMessage());
                }
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Message sent successfully. Key: {}, value: {}. Partition: {}",
                        key, value, result.getRecordMetadata().partition());
            }
        };
    }

    /**
     * Sends Kafka event synchronously using Kafka and the default topic
     *
     * @param libraryEvent event to be sent by Kafka
     * @return send result
     * @throws JsonProcessingException if event cannot be converted to JSON
     * @throws ExecutionException if there is an error getting the result
     * @throws InterruptedException if there is an error getting the result
     * @throws TimeoutException if there is an error getting the result
     */
    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        SendResult<Integer, String> sendResult;
        try {
            // .get() waits until future is complete (success or failure)
            // callback is executed in the same thread
            sendResult = kafkaTemplate.sendDefault(key, value).get(1L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error sending message. Key: {}, value: {}", key, value);
            throw e;
        }

        return sendResult;
    }

    public void sendLibraryEventUsingProducerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);

        // producerRecords has the topic data, among with key, value and other metadata
        ListenableFuture<SendResult<Integer, String>> resultFuture = kafkaTemplate.send(producerRecord);

        // Callback executed in a different thread
        resultFuture.addCallback(loggingCallback(key, value));
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {

        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }
}
