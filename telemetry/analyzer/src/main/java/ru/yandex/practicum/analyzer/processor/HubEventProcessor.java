package ru.yandex.practicum.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.broker.AnalyzerTopics;
import ru.yandex.practicum.analyzer.config.HubEventConsumerConfig;
import ru.yandex.practicum.analyzer.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HubEventProcessor implements Runnable {
    private final HubEventConsumerConfig consumerConfig;
    private final Map<Class<?>, HubEventHandler<?>> hubEventHandlers;
    private KafkaConsumer<String, HubEventAvro> consumer;

    public HubEventProcessor(HubEventConsumerConfig consumerConfig, Set<HubEventHandler<?>> hubEventHandlers) {
        this.consumerConfig = consumerConfig;
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getPayloadType,
                        Function.identity()
                ));
    }

    @Override
    public void run() {
        try {
            Properties properties = new Properties();
            properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    consumerConfig.getBootstrapServers());
            properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getHubEventConsumer().getKeyDeserializer());
            properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getHubEventConsumer().getValueDeserializer());
            properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    consumerConfig.getHubEventConsumer().getAutoOffsetReset());
            properties.put(ConsumerConfig.GROUP_ID_CONFIG,
                    consumerConfig.getHubEventConsumer().getGroupId());

            this.consumer = new KafkaConsumer<>(properties);

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(List.of(AnalyzerTopics.TELEMETRY_HUBS_V1));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    log.info("Поступили данные события хаба: {}", record.value());
                    handleEvent(record.value());
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен сигнал остановки (WakeupException)");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хаба", e);
        } finally {
            try {
                log.info("Фиксация смещений перед закрытием...");
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
            }
        }
    }

    private void handleEvent(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        Object payload = hubEvent.getPayload();

        if (payload == null) {
            log.error("Получено событие хаба без payload: {}", hubEvent);
            return;
        }

        HubEventHandler<Object> handler = (HubEventHandler<Object>) hubEventHandlers.get(payload.getClass());

        if (handler != null) {
            handler.handle(hubId, payload);
        } else {
            log.error("Получено событие хаба неизвестного типа: {}", hubEvent);
        }
    }

}