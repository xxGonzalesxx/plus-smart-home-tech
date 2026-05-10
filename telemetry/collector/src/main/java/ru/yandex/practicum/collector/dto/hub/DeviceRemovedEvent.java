package ru.yandex.practicum.collector.dto.hub;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceRemovedEvent extends HubEvent {
    private String id;
    private String type = "DEVICE_REMOVED";

    @Override
    public String getType() {
        return type;
    }
}