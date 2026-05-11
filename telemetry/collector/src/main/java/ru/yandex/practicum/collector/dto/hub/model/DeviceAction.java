package ru.yandex.practicum.collector.dto.hub.model;

import lombok.Data;

@Data
public class DeviceAction {
    private String sensorId;
    private ActionType type;    // ACTIVATE, DEACTIVATE, INVERSE, SET_VALUE
    private Integer value;
}