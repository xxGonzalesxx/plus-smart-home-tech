package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.entity.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

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

        if (payload instanceof DeviceAddedEventAvro) {
            handleDeviceAdded((DeviceAddedEventAvro) payload, event.getHubId());
        } else if (payload instanceof DeviceRemovedEventAvro) {
            handleDeviceRemoved((DeviceRemovedEventAvro) payload, event.getHubId());
        } else if (payload instanceof ScenarioAddedEventAvro) {
            handleScenarioAdded((ScenarioAddedEventAvro) payload, event.getHubId());
        } else if (payload instanceof ScenarioRemovedEventAvro) {
            handleScenarioRemoved((ScenarioRemovedEventAvro) payload, event.getHubId());
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
        // Проверяем, существует ли уже такой сценарий
        if (scenarioRepository.findByHubIdAndName(hubId, event.getName()).isPresent()) {
            log.warn("Scenario already exists: hubId={}, name={}", hubId, event.getName());
            return;
        }

        // Сохраняем условия
        var conditions = event.getConditions().stream()
                .map(cond -> Condition.builder()
                        .sensorId(cond.getSensorId())
                        .type(ru.yandex.practicum.analyzer.model.enums.ConditionType.valueOf(cond.getType().name()))
                        .operation(ru.yandex.practicum.analyzer.model.enums.ConditionOperation.valueOf(cond.getOperation().name()))
                        .value(cond.getValue() instanceof Integer ? (Integer) cond.getValue() : null)
                        .build())
                .collect(Collectors.toList());
        conditionRepository.saveAll(conditions);

        // Сохраняем действия
        var actions = event.getActions().stream()
                .map(act -> Action.builder()
                        .sensorId(act.getSensorId())
                        .type(ru.yandex.practicum.analyzer.model.enums.ActionType.valueOf(act.getType().name()))
                        .value(act.getValue())
                        .build())
                .collect(Collectors.toList());
        actionRepository.saveAll(actions);

        // Создаём сценарий
        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .conditions(conditions)
                .actions(actions)
                .build();
        scenarioRepository.save(scenario);

        log.info("Scenario added: hubId={}, name={}, conditions={}, actions={}",
                hubId, event.getName(), conditions.size(), actions.size());
    }

    private void handleScenarioRemoved(ScenarioRemovedEventAvro event, String hubId) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Scenario removed: hubId={}, name={}", hubId, event.getName());
                });
    }
}