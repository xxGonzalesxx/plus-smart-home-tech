package ru.yandex.practicum.collector.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.dto.sensor.SensorSnapshot;
import ru.yandex.practicum.collector.processor.SnapshotProcessor;

import java.util.List;

/**
 * Kafka consumer для обработки снимков датчиков из топика telemetry.snapshots.v1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotConsumer {

    private final SnapshotProcessor snapshotProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "telemetry.snapshots.v1",
            groupId = "analyzer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSnapshot(SensorSnapshot snapshot) {
        log.info("📥 Received snapshot from Kafka: hubId={}, timestamp={}, sensors count={}",
                snapshot.getHubId(), snapshot.getTimestamp(), 
                snapshot.getSensors() != null ? snapshot.getSensors().size() : 0);

        try {
            // Передаём снимок на обработку
            if (snapshot.getSensors() != null && !snapshot.getSensors().isEmpty()) {
                snapshotProcessor.processSnapshot(snapshot.getHubId(), snapshot.getSensors());
            } else {
                log.warn("⚠️ Snapshot has no sensor data: hubId={}", snapshot.getHubId());
            }
        } catch (Exception e) {
            log.error("❌ Error processing snapshot: {}", e.getMessage(), e);
        }
    }
}
