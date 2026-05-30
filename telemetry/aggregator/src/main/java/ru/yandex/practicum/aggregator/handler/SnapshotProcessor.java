package ru.yandex.practicum.aggregator.handler;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotProcessor {

    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event);

}