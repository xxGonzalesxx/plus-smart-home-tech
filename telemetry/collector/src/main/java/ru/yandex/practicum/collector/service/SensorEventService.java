package ru.yandex.practicum.collector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "telemetry.sensors.v1";

    public void send(SensorEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.getHubId(), json);
            log.debug("Sent sensor event to Kafka: hubId={}, json={}", event.getHubId(), json);
        } catch (Exception e) {
            log.error("Failed to send sensor event to Kafka", e);
        }
    }
}