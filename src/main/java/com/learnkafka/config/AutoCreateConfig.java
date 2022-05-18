package com.learnkafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration for automatic creation of topics. NOT RECOMMENDED FOR PRODUCTION
 *
 * @author Alvaro Pedraza
 */
@Configuration
@Profile("local")
public class AutoCreateConfig {

    @Bean
    public NewTopic libraryEvents() {
        return TopicBuilder.name("library-events")
                .partitions(3)
                .replicas(3) // actual clusters: 9092/3/4
                .build();
    }
}
