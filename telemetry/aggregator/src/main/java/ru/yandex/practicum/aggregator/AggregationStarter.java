package ru.yandex.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.broker.AggregatorTopics;
import ru.yandex.practicum.aggregator.config.AggregatorConsumerConfig;
import ru.yandex.practicum.aggregator.handler.SnapshotProcessor;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStarter {

    private final AggregatorConsumerConfig consumerConfig;
    private final SnapshotProcessor handler;
    private final KafkaTemplate<String, SpecificRecordBase> producer;
    private KafkaConsumer<String, SensorEventAvro> consumer;

    public void start() {
        try {
            Properties props = new Properties();
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    consumerConfig.getBootstrapServers());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getConsumer().getKeyDeserializer());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getConsumer().getValueDeserializer());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    consumerConfig.getConsumer().getAutoOffsetReset());
            props.put(ConsumerConfig.GROUP_ID_CONFIG,
                    consumerConfig.getConsumer().getGroupId());

            this.consumer = new KafkaConsumer<>(props);

            consumer.subscribe(List.of(AggregatorTopics.TELEMETRY_SENSORS_V1));


            while (true) {

                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Поступили данные события датчика: {}", record.value());
                    Optional<SensorsSnapshotAvro> result = handler.updateState(record.value());
                    if (result.isPresent()) {
                        SensorsSnapshotAvro snapshotAvro = result.get();
                        String key = snapshotAvro.getHubId();
                        log.info("Готовы данные снимка состояния датчиков (snapshot) в формате Avro:" +
                                        " >>> {} <<< для отправки в Kafka-топик: >>> {} <<<",
                                snapshotAvro, AggregatorTopics.TELEMETRY_SNAPSHOTS_V1);

                        producer.send(AggregatorTopics.TELEMETRY_SNAPSHOTS_V1, key, snapshotAvro);
                    }
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал остановки (WakeupException)");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {

                log.info("Сброс буферов и фиксация смещений перед закрытием...");

                if (producer != null) {
                    producer.flush();
                }

                if (consumer != null) {
                    consumer.commitSync();
                }

            } catch (Exception e) {
                log.error("Ошибка при финальном сбросе данных или коммите оффсетов", e);
            } finally {
                if (consumer != null) {
                    log.info("Закрываем консьюмер");
                    consumer.close();
                }

                if (producer != null) {
                    log.info("Закрываем продюсер");
                    producer.destroy();
                }
            }
        }
    }
}