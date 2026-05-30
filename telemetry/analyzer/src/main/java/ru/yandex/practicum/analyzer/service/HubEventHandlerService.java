package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.entity.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventHandlerService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void processHubEvent(HubEventAvro event) {
        Object payload = event.getPayload();
        String hubId = event.getHubId();

        if (payload instanceof DeviceAddedEventAvro) {
            handleDeviceAdded((DeviceAddedEventAvro) payload, hubId);
        } else if (payload instanceof DeviceRemovedEventAvro) {
            handleDeviceRemoved((DeviceRemovedEventAvro) payload, hubId);
        } else if (payload instanceof ScenarioAddedEventAvro) {
            handleScenarioAdded((ScenarioAddedEventAvro) payload, hubId);
        } else if (payload instanceof ScenarioRemovedEventAvro) {
            handleScenarioRemoved((ScenarioRemovedEventAvro) payload, hubId);
        } else {
            log.warn("Unknown hub event type: {}", payload.getClass().getSimpleName());
        }
    }

    private void handleDeviceAdded(DeviceAddedEventAvro event, String hubId) {
        Sensor sensor = Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();
        sensorRepository.save(sensor);
        log.info("Device added: id={}, hubId={}", event.getId(), hubId);
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro event, String hubId) {
        sensorRepository.findByIdAndHubId(event.getId(), hubId)
                .ifPresent(sensor -> {
                    sensorRepository.delete(sensor);
                    log.info("Device removed: id={}, hubId={}", event.getId(), hubId);
                });
    }

    @Transactional
    public void handleScenarioAdded(ScenarioAddedEventAvro event, String hubId) {
        if (scenarioRepository.findByHubIdAndName(hubId, event.getName()).isPresent()) {
            log.warn("Scenario already exists: hubId={}, name={}", hubId, event.getName());
            return;
        }

        // Создаём сценарий
        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .build();
        scenario = scenarioRepository.save(scenario);

        // Сохраняем условия и связываем со сценарием
        List<ScenarioCondition> scenarioConditions = new ArrayList<>();
        for (var cond : event.getConditions()) {
            Condition condition = Condition.builder()
                    .type(ru.yandex.practicum.analyzer.model.enums.ConditionType.valueOf(cond.getType().name()))
                    .operation(ru.yandex.practicum.analyzer.model.enums.ConditionOperation.valueOf(cond.getOperation().name()))
                    .value(cond.getValue() instanceof Integer ? (Integer) cond.getValue() : 0)
                    .build();
            condition = conditionRepository.save(condition);

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .scenario(scenario)
                    .sensorId(cond.getSensorId())
                    .condition(condition)
                    .build();
            scenarioConditions.add(scenarioCondition);
        }

        // Сохраняем действия и связываем со сценарием
        List<ScenarioAction> scenarioActions = new ArrayList<>();
        for (var act : event.getActions()) {
            Action action = Action.builder()
                    .type(ru.yandex.practicum.analyzer.model.enums.ActionType.valueOf(act.getType().name()))
                    .value(act.getValue())
                    .build();
            action = actionRepository.save(action);

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .scenario(scenario)
                    .sensorId(act.getSensorId())
                    .action(action)
                    .build();
            scenarioActions.add(scenarioAction);
        }

        scenario.setConditions(scenarioConditions);
        scenario.setActions(scenarioActions);
        scenarioRepository.save(scenario);

        log.info("Scenario added: hubId={}, name={}, conditions={}, actions={}",
                hubId, event.getName(), scenarioConditions.size(), scenarioActions.size());
    }

    private void handleScenarioRemoved(ScenarioRemovedEventAvro event, String hubId) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Scenario removed: hubId={}, name={}", hubId, event.getName());
                });
    }
}