package ru.yandex.practicum.analyzer.processor;

import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.*;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final ScenarioRepository scenarioRepository;

    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        log.info("Processing snapshot for hub: {}", hubId);

        // Получаем все сценарии для хаба
        var scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.debug("No scenarios for hub: {}", hubId);
            return;
        }

        // Получаем состояние сенсоров
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        // Проверяем каждый сценарий
        for (Scenario scenario : scenarios) {
            log.debug("Checking scenario: {}", scenario.getName());

            boolean allConditionsMet = checkConditions(scenario.getConditions(), sensorsState);

            if (allConditionsMet) {
                log.info("✅ Scenario triggered: {}", scenario.getName());
                executeActions(scenario.getActions(), hubId, scenario.getName());
            }
        }
    }

    private boolean checkConditions(java.util.List<ScenarioCondition> conditions,
                                    Map<String, SensorStateAvro> sensorsState) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (ScenarioCondition sc : conditions) {
            String sensorId = sc.getSensorId();
            SensorStateAvro sensorState = sensorsState.get(sensorId);

            if (sensorState == null) {
                log.debug("Sensor not found: {}", sensorId);
                return false;
            }

            Condition condition = sc.getCondition();
            if (condition == null) {
                log.debug("Condition is null for sensor: {}", sensorId);
                return false;
            }

            // Получаем значение из сенсора в зависимости от типа
            Integer actualValue = extractSensorValue(sensorState.getData(), condition.getType());
            if (actualValue == null) {
                log.debug("Cannot extract value from sensor: {}", sensorId);
                return false;
            }

            boolean conditionMet = evaluateCondition(actualValue, condition.getValue(), condition.getOperation());
            if (!conditionMet) {
                log.debug("Condition not met: sensorId={}, actual={}, expected={}, operation={}",
                        sensorId, actualValue, condition.getValue(), condition.getOperation());
                return false;
            }
        }
        return true;
    }

    private Integer extractSensorValue(Object sensorData,
                                       ru.yandex.practicum.analyzer.model.enums.ConditionType conditionType) {
        if (sensorData == null) {
            return null;
        }

        // Определяем тип сенсора и извлекаем нужное значение
        switch (sensorData) {
            case ClimateSensorAvro climate -> {
                return switch (conditionType) {
                    case TEMPERATURE -> climate.getTemperatureC();
                    case HUMIDITY -> climate.getHumidity();
                    case CO2LEVEL -> climate.getCo2Level();
                    default -> null;
                };
            }
            case LightSensorAvro light -> {
                if (conditionType == ConditionType.LUMINOSITY) {
                    return light.getLuminosity();
                }
                return null;
            }
            case MotionSensorAvro motion -> {
                if (conditionType == ConditionType.MOTION) {
                    return motion.getMotion() ? 1 : 0;
                }
                return null;
            }
            case SwitchSensorAvro switchSensor -> {
                if (conditionType == ConditionType.SWITCH) {
                    return switchSensor.getState() ? 1 : 0;
                }
                return null;
            }
            case TemperatureSensorAvro temp -> {
                if (conditionType == ConditionType.TEMPERATURE) {
                    return temp.getTemperatureC();
                }
                return null;
            }
            default -> {
                log.warn("Unknown sensor data type: {}", sensorData.getClass().getSimpleName());
                return null;
            }
        }
    }

    private boolean evaluateCondition(Integer actual, Integer expected,
                                      ru.yandex.practicum.analyzer.model.enums.ConditionOperation operation) {
        if (actual == null || expected == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    private void executeActions(java.util.List<ScenarioAction> actions, String hubId, String scenarioName) {
        if (actions == null || actions.isEmpty()) {
            log.warn("No actions to execute for scenario: {}", scenarioName);
            return;
        }

        log.info("Executing {} actions for scenario '{}' on hub '{}'",
                actions.size(), scenarioName, hubId);

        for (ScenarioAction sa : actions) {
            Action action = sa.getAction();
            if (action != null) {
                log.info("  📤 Action: sensorId={}, type={}, value={}",
                        sa.getSensorId(), action.getType(), action.getValue());
                // TODO: Отправить gRPC команду в Hub Router
                // sendCommand(hubId, scenarioName, sa.getSensorId(), action);
            }
        }
    }
}