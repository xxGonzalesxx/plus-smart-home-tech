package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@UtilityClass
public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent event) {
        if (event == null) return null;

        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        if (event instanceof LightSensorEvent) {
            LightSensorEvent light = (LightSensorEvent) event;
            builder.setPayload(LightSensorAvro.newBuilder()
                    .setLinkQuality(light.getLinkQuality())
                    .setLuminosity(light.getLuminosity())
                    .build());
        } else if (event instanceof SwitchSensorEvent) {
            SwitchSensorEvent sw = (SwitchSensorEvent) event;
            builder.setPayload(SwitchSensorAvro.newBuilder()
                    .setState(sw.getState())
                    .build());
        } else if (event instanceof MotionSensorEvent) {
            MotionSensorEvent motion = (MotionSensorEvent) event;
            builder.setPayload(MotionSensorAvro.newBuilder()
                    .setLinkQuality(motion.getLinkQuality())
                    .setMotion(motion.getMotion())
                    .setVoltage(motion.getVoltage())
                    .build());
        } else if (event instanceof TemperatureSensorEvent) {
            TemperatureSensorEvent temp = (TemperatureSensorEvent) event;
            builder.setPayload(TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(temp.getTemperatureC())
                    .setTemperatureF(temp.getTemperatureF())
                    .build());
        } else if (event instanceof ClimateSensorEvent) {
            ClimateSensorEvent climate = (ClimateSensorEvent) event;
            builder.setPayload(ClimateSensorAvro.newBuilder()
                    .setTemperatureC(climate.getTemperatureC())
                    .setHumidity(climate.getHumidity())
                    .setCo2Level(climate.getCo2Level())
                    .build());
        }

        return builder.build();
    }
}