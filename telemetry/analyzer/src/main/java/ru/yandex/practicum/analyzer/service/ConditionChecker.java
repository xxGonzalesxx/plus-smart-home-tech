package ru.yandex.practicum.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
public class ConditionChecker {

    public boolean checkCondition(Condition condition, String sensorId, SensorsSnapshotAvro snapshot) {
        SensorStateAvro sensorState = snapshot.getSensorsState().get(sensorId);

        if (sensorState == null) {
            log.debug("Sensor {} not found in snapshot", sensorId);
            return false;
        }

        ConditionType type = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer expectedValue = condition.getValue();

        Object actualValue = getActualValue(sensorState.getData(), type);

        if (actualValue == null) {
            log.debug("Could not get value for sensor {} of type {}", sensorId, type);
            return false;
        }

        return compare(actualValue, expectedValue, operation);
    }

    private Object getActualValue(Object data, ConditionType type) {
        return switch (type) {
            case MOTION -> {
                if (data instanceof MotionSensorAvro motion) yield motion.getMotion();
                yield null;
            }
            case LUMINOSITY -> {
                if (data instanceof LightSensorAvro light) yield light.getLuminosity();
                yield null;
            }
            case SWITCH -> {
                if (data instanceof SwitchSensorAvro switchSensor) yield switchSensor.getState();
                yield null;
            }
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro temp) yield temp.getTemperatureC();
                if (data instanceof ClimateSensorAvro climate) yield climate.getTemperatureC();
                yield null;
            }
            case CO2LEVEL -> {
                if (data instanceof ClimateSensorAvro climate) yield climate.getCo2Level();
                yield null;
            }
            case HUMIDITY -> {
                if (data instanceof ClimateSensorAvro climate) yield climate.getHumidity();
                yield null;
            }
        };
    }

    private boolean compare(Object actual, Integer expected, ConditionOperation operation) {
        if (actual == null) return false;

        int actualValue;
        if (actual instanceof Boolean bool) {
            actualValue = bool ? 1 : 0;
        } else if (actual instanceof Integer intVal) {
            actualValue = intVal;
        } else {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actualValue == expected;
            case GREATER_THAN -> actualValue > expected;
            case LOWER_THAN -> actualValue < expected;
        };
    }
}