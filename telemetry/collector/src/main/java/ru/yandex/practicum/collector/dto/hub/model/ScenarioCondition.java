package ru.yandex.practicum.collector.dto.hub.model;

import lombok.Data;

@Data
public class ScenarioCondition {
    private String sensorId;
    private String type;        // MOTION, LUMINOSITY, SWITCH, TEMPERATURE, CO2LEVEL, HUMIDITY
    private String operation;   // EQUALS, GREATER_THAN, LOWER_THAN
    private Integer value;
}