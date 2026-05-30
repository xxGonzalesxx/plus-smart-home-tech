package ru.yandex.practicum.collector.processor;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.collector.entity.Scenario;
import ru.yandex.practicum.collector.entity.ScenarioAction;
import ru.yandex.practicum.collector.entity.ScenarioCondition;
import ru.yandex.practicum.collector.service.ScenarioService;
import ru.yandex.practicum.hub.router.grpc.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Обрабатывает снимки датчиков (snapshots) и проверяет условия сценариев.
 * При выполнении всех условий отправляет команды в Hub Router через gRPC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final ScenarioService scenarioService;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    /**
     * Обработать снимок датчиков и проверить сценарии
     */
    public void processSnapshot(String hubId, List<SensorEvent> sensorEvents) {
        log.info("📊 Processing snapshot for hub: {}, sensor events count: {}", hubId, sensorEvents.size());

        try {
            // Получаем все активные сценарии для этого хаба
            List<Scenario> scenarios = scenarioService.getActiveScenarios(hubId);
            log.debug("Found {} active scenarios for hub {}", scenarios.size(), hubId);

            // Проверяем каждый сценарий
            for (Scenario scenario : scenarios) {
                if (evaluateScenario(scenario, sensorEvents)) {
                    log.info("✓ Scenario conditions met: id={}, name={}", scenario.getId(), scenario.getName());
                    executeScenarioActions(hubId, scenario, sensorEvents);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error processing snapshot: {}", e.getMessage(), e);
        }
    }

    /**
     * Проверить, выполнены ли все условия сценария
     */
    private boolean evaluateScenario(Scenario scenario, List<SensorEvent> sensorEvents) {
        if (scenario.getConditions() == null || scenario.getConditions().isEmpty()) {
            log.warn("⚠️ Scenario has no conditions: {}", scenario.getName());
            return false;
        }

        log.debug("🔍 Evaluating scenario: {}, conditions count: {}",
                scenario.getName(), scenario.getConditions().size());

        // Все условия должны быть выполнены (AND логика)
        return scenario.getConditions().stream()
                .allMatch(condition -> evaluateCondition(condition, sensorEvents));
    }

    /**
     * Проверить одно условие против снимка датчика
     */
    private boolean evaluateCondition(ScenarioCondition condition, List<SensorEvent> sensorEvents) {
        String sensorId = condition.getSensorId();

        // Найти событие датчика с нужным ID
        SensorEvent sensorEvent = sensorEvents.stream()
                .filter(event -> sensorId.equals(event.getId()))
                .findFirst()
                .orElse(null);

        if (sensorEvent == null) {
            log.debug("⚠️ Sensor event not found: {}", sensorId);
            return false;
        }

        Integer sensorValue = extractSensorValue(sensorEvent, condition.getType());
        if (sensorValue == null) {
            log.debug("⚠️ Could not extract value from sensor: {}", sensorId);
            return false;
        }

        boolean result = compareValue(sensorValue, condition.getValue(), condition.getOperation());
        log.debug("Condition evaluation: sensor={}, type={}, value={}, operation={}, conditionValue={}, result={}",
                sensorId, condition.getType(), sensorValue, condition.getOperation(), condition.getValue(), result);

        return result;
    }

    /**
     * Извлечь значение из события датчика
     */
    private Integer extractSensorValue(SensorEvent event, ru.yandex.practicum.collector.dto.hub.model.ConditionType conditionType) {
        if (event instanceof MotionSensorEvent) {
            MotionSensorEvent motionEvent = (MotionSensorEvent) event;
            if (conditionType == ru.yandex.practicum.collector.dto.hub.model.ConditionType.MOTION) {
                return motionEvent.getMotion() != null && motionEvent.getMotion() ? 1 : 0;
            }
        } else if (event instanceof LightSensorEvent) {
            LightSensorEvent lightEvent = (LightSensorEvent) event;
            if (conditionType == ru.yandex.practicum.collector.dto.hub.model.ConditionType.LUMINOSITY) {
                return lightEvent.getLuminosity();
            }
        } else if (event instanceof SwitchSensorEvent) {
            SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
            if (conditionType == ru.yandex.practicum.collector.dto.hub.model.ConditionType.SWITCH) {
                return switchEvent.getState() != null && switchEvent.getState() ? 1 : 0;
            }
        } else if (event instanceof TemperatureSensorEvent) {
            TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;
            if (conditionType == ru.yandex.practicum.collector.dto.hub.model.ConditionType.TEMPERATURE) {
                return tempEvent.getTemperatureC();
            }
        } else if (event instanceof ClimateSensorEvent) {
            ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
            switch (conditionType) {
                case TEMPERATURE:
                    return climateEvent.getTemperatureC();
                case HUMIDITY:
                    return climateEvent.getHumidity();
                case CO2LEVEL:
                    return climateEvent.getCo2Level();
            }
        }
        return null;
    }

    /**
     * Сравнить значение по операции
     */
    private boolean compareValue(Integer actual, Integer expected,
                                  ru.yandex.practicum.collector.dto.hub.model.ConditionOperation operation) {
        if (actual == null || expected == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    /**
     * Выполнить действия сценария через gRPC
     */
    private void executeScenarioActions(String hubId, Scenario scenario, List<SensorEvent> sensorEvents) {
        if (scenario.getActions() == null || scenario.getActions().isEmpty()) {
            log.warn("⚠️ Scenario has no actions: {}", scenario.getName());
            return;
        }

        log.info("⚡ Executing scenario actions: id={}, name={}, actions count={}",
                scenario.getId(), scenario.getName(), scenario.getActions().size());

        try {
            // Преобразуем действия в gRPC запросы
            List<DeviceActionRequest> actionRequests = scenario.getActions().stream()
                    .map(action -> convertToGrpcAction(action))
                    .collect(Collectors.toList());

            // Отправляем команду в Hub Router
            ExecuteScenarioRequest request = ExecuteScenarioRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioId(String.valueOf(scenario.getId()))
                    .setScenarioName(scenario.getName())
                    .addAllActions(actionRequests)
                    .build();

            log.debug("📤 Sending gRPC request to Hub Router: hubId={}, scenarioId={}, actions={}",
                    hubId, scenario.getId(), actionRequests.size());

            ExecuteScenarioResponse response = hubRouterStub.executeScenario(request);
            log.info("✅ Scenario executed successfully: id={}, name={}, success={}",
                    scenario.getId(), scenario.getName(), response.getSuccess());

            if (!response.getSuccess()) {
                log.warn("⚠️ Scenario execution returned false: message={}", response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.UNAVAILABLE) {
                log.error("❌ Hub Router is unavailable: {}", e.getMessage());
            } else if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                log.error("❌ Hub Router request timeout: {}", e.getMessage());
            } else {
                log.error("❌ gRPC error from Hub Router: status={}, message={}",
                        e.getStatus().getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("❌ Error executing scenario actions: {}", e.getMessage(), e);
        }
    }

    /**
     * Преобразовать ScenarioAction в DeviceActionRequest для gRPC
     */
    private DeviceActionRequest convertToGrpcAction(ScenarioAction action) {
        DeviceActionRequest.Builder builder = DeviceActionRequest.newBuilder()
                .setSensorId(action.getSensorId())
                .setActionType(convertActionType(action.getType()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }

    /**
     * Преобразовать ActionType в gRPC ActionType
     */
    private ru.yandex.practicum.hub.router.grpc.ActionType convertActionType(
            ru.yandex.practicum.collector.dto.hub.model.ActionType type) {
        return switch (type) {
            case ACTIVATE -> ru.yandex.practicum.hub.router.grpc.ActionType.ACTIVATE;
            case DEACTIVATE -> ru.yandex.practicum.hub.router.grpc.ActionType.DEACTIVATE;
            case INVERSE -> ru.yandex.practicum.hub.router.grpc.ActionType.INVERSE;
            case SET_VALUE -> ru.yandex.practicum.hub.router.grpc.ActionType.SET_VALUE;
        };
    }
}
