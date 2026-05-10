package ru.yandex.practicum.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {
    private Boolean state;
    private String type = "SWITCH_SENSOR_EVENT";

    @Override
    public String getType() {
        return type;
    }
}