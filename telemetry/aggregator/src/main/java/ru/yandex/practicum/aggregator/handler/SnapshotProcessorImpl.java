package ru.yandex.practicum.aggregator.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class SnapshotProcessorImpl implements SnapshotProcessor {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId().toString();
        String sensorId = event.getId().toString();
        long eventTimestamp = event.getTimestamp();  // ← long

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        // 1. Если снапшота для этого хаба нет — создаём новый
        if (snapshot == null) {
            log.info("Создаём новый снапшот для хаба {}", hubId);

            SensorStateAvro state = SensorStateAvro.newBuilder()
                    .setTimestamp(eventTimestamp)  // ← long
                    .setData(event.getPayload())
                    .build();

            Map<String, SensorStateAvro> initialMap = new HashMap<>();
            initialMap.put(sensorId, state);

            SensorsSnapshotAvro newSnapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(eventTimestamp)  // ← long
                    .setSensorsState(initialMap)
                    .build();

            snapshots.put(hubId, newSnapshot);
            return Optional.of(newSnapshot);
        }

        // 2. Проверяем, есть ли данные для этого датчика
        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);

        // Если данные не изменились — пропускаем
        if (oldState != null && oldState.getData().equals(event.getPayload())) {
            log.debug("Данные датчика {} в хабе {} не изменились", sensorId, hubId);
            return Optional.empty();
        }

        // 3. Обновляем данные датчика
        log.info("Обновляем данные датчика {} в снапшоте хаба {}", sensorId, hubId);

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)  // ← long
                .setData(event.getPayload())
                .build();

        // Важно: создаём новую карту, чтобы избежать проблем с неизменяемостью
        Map<String, SensorStateAvro> updatedSensors = new HashMap<>(snapshot.getSensorsState());
        updatedSensors.put(sensorId, newState);

        // Обновляем снапшот
        snapshot.setSensorsState(updatedSensors);
        snapshot.setTimestamp(eventTimestamp);  // ← long

        return Optional.of(snapshot);
    }
}