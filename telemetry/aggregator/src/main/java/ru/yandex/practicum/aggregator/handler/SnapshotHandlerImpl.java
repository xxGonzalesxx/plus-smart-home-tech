package ru.yandex.practicum.aggregator.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotHandlerImpl implements SnapshotProcessor {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot;

        if (snapshots.containsKey(event.getHubId())) {
            snapshot = snapshots.get(event.getHubId());
            log.info("Найден снимок состояния для хаба с ID: {}.", event.getHubId());

            SensorStateAvro oldState;
            if (snapshot.getSensorsState().containsKey(event.getId())) {
                oldState = snapshot.getSensorsState().get(event.getId());
                if (oldState.getTimestamp().isAfter(event.getTimestamp())
                        || oldState.getData().equals(event.getPayload())) {
                    log.info("Состояние снимка датчиков хаба с ID: {} не изменилось.", event.getHubId());
                    return Optional.empty();
                }
            }

        } else {
            SensorStateAvro state = SensorStateAvro.newBuilder()
                    .setTimestamp(event.getTimestamp())
                    .setData(event.getPayload())
                    .build();

            Map<String, SensorStateAvro> initialMap = new HashMap<>();
            initialMap.put(event.getId(), state);

            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(Instant.now())
                    .setSensorsState(initialMap)
                    .build();
            snapshots.put(event.getHubId(), snapshot);
            log.info("Снимок состояния для хаба с ID: {} не найден. Создаем новый снимок.", event.getHubId());
            return Optional.of(snapshot);
        }

        log.info("Обновляем данные снимка состояния для хаба с ID: {}.", event.getHubId());
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        Map<String, SensorStateAvro> mutableMap = new HashMap<>(snapshot.getSensorsState());
        mutableMap.put(event.getId(), newState);

        snapshot.setSensorsState(mutableMap);
        snapshot.setTimestamp(event.getTimestamp());
        return Optional.of(snapshot);
    }

}