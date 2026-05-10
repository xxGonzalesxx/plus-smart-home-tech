package ru.yandex.practicum.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LightSensorEvent extends SensorEvent {
    private Integer linkQuality;
    private Integer luminosity;
    private String type = "LIGHT_SENSOR_EVENT";

    @Override
    public String getType() {
        return type;
    }
}