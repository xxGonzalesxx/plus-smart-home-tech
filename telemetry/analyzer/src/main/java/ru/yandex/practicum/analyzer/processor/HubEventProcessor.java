package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.deserializer.HubEventDeserializer;
import ru.yandex.practicum.analyzer.service.HubEventHandlerService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final HubEventHandlerService hubEventHandlerService;

    private KafkaConsumer<String, HubEventAvro> consumer;

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-hub-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("telemetry.hubs.v1"));

        log.info("HubEventProcessor started, polling for hub events...");

        try {
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    HubEventAvro event = record.value();
                    log.info("Received hub event: hubId={}", event.getHubId());
                    hubEventHandlerService.processHubEvent(event);
                });
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor stopped");
        } catch (Exception e) {
            log.error("Error in HubEventProcessor", e);
        } finally {
            consumer.close();
            log.info("HubEventProcessor closed");
        }
    }
}