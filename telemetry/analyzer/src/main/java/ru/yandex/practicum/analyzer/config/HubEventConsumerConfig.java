package ru.yandex.practicum.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("custom.kafka-hub-event")
public class HubEventConsumerConfig {

    @Value("${custom.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private HubEventConsumer hubEventConsumer = new HubEventConsumer();

    @Getter
    @Setter
    public static class HubEventConsumer {
        private String keyDeserializer;
        private String valueDeserializer;
        private String autoOffsetReset;
        private String groupId;
    }

}