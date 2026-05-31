package ru.yandex.practicum.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("custom.kafka-snapshot")
public class SnapshotConsumerConfig {

    @Value("${custom.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private SnapshotConsumer snapshotConsumer = new SnapshotConsumer();

    @Getter
    @Setter
    public static class SnapshotConsumer {
        private String keyDeserializer;
        private String valueDeserializer;
        private String autoOffsetReset;
        private String groupId;
    }

}