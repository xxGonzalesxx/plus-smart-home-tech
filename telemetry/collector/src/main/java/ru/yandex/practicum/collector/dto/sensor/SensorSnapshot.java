package ru.yandex.practicum.collector.dto.sensor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Снимок состояния всех датчиков хаба
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorSnapshot {
    private String hubId;
    private Instant timestamp;
    private List<SensorEvent> sensors;
}
