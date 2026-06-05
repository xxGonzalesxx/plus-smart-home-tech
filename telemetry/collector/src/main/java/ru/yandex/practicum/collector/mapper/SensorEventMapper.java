package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.collector.dto.sensor.LightSensorEvent;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.collector.dto.sensor.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Slf4j
@UtilityClass
public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent event) {
        if (event == null) {
            log.warn("SensorEvent is null");
            return null;
        }

        try {
            SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                    .setId(event.getId())
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp());

            if (event instanceof LightSensorEvent) {
                LightSensorEvent light = (LightSensorEvent) event;
                builder.setPayload(LightSensorAvro.newBuilder()
                        .setLinkQuality(light.getLinkQuality() != null ? light.getLinkQuality() : 0)
                        .setLuminosity(light.getLuminosity() != null ? light.getLuminosity() : 0)
                        .build());
            } else if (event instanceof SwitchSensorEvent) {
                SwitchSensorEvent sw = (SwitchSensorEvent) event;
                builder.setPayload(SwitchSensorAvro.newBuilder()
                        .setState(sw.getState() != null && sw.getState())
                        .build());
            }
            else {
                log.error("Unknown sensor event type: {}", event.getClass().getSimpleName());
                throw new IllegalArgumentException("Unknown sensor event type");
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to convert sensor event to Avro: id={}, type={}",
                    event.getId(), event.getClass().getSimpleName(), e);
            throw new RuntimeException("Cannot convert sensor event", e);
        }
    }
}