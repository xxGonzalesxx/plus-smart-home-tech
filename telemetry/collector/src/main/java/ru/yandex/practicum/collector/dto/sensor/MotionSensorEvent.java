package ru.yandex.practicum.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    private Integer linkQuality;
    private Boolean motion;
    private Integer voltage;
    private String type = "MOTION_SENSOR_EVENT";

    @Override
    public String getType() {
        return type;
    }
}