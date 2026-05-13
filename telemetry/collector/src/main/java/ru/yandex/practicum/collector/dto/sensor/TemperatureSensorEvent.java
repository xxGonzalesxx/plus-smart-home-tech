package ru.yandex.practicum.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemperatureSensorEvent extends SensorEvent {
    private Integer temperatureC;
    private Integer temperatureF;
    private String type = "TEMPERATURE_SENSOR_EVENT";

    @Override
    public String getType() {
        return type;
    }
}