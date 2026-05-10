package ru.yandex.practicum.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    private Integer temperatureC;
    private Integer humidity;
    private Integer co2Level;
    private String type = "CLIMATE_SENSOR_EVENT";

    @Override
    public String getType() {
        return type;
    }
}