package ru.yandex.practicum.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "custom.kafka-hub-event")
public class HubEventConsumerConfig {
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