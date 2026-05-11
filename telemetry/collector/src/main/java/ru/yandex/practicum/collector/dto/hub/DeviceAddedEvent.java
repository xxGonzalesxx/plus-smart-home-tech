package ru.yandex.practicum.collector.dto.hub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.collector.dto.hub.model.DeviceType;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private DeviceType deviceType; // MOTION_SENSOR, TEMPERATURE_SENSOR, LIGHT_SENSOR, CLIMATE_SENSOR, SWITCH_SENSOR
    private String type = "DEVICE_ADDED";

    @Override
    public String getType() {
        return type;
    }
}