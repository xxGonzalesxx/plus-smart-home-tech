package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.model.enums.ActionType;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Override
    public Class<ScenarioAddedEventAvro> getPayloadType() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    public void handle(String hubId, ScenarioAddedEventAvro event) {
        if (scenarioRepository.findByHubIdAndName(hubId, event.getName()).isPresent()) {
            log.info("Scenario '{}' already exists in hub {}", event.getName(), hubId);
            return;
        }

        var conditions = event.getConditions().stream()
                .map(cond -> Condition.builder()
                        .sensorId(cond.getSensorId())
                        .type(ConditionType.valueOf(cond.getType().name()))
                        .operation(ConditionOperation.valueOf(cond.getOperation().name()))
                        .value(cond.getValue() instanceof Integer ? (Integer) cond.getValue() : null)
                        .build())
                .collect(Collectors.toList());
        conditionRepository.saveAll(conditions);

        var actions = event.getActions().stream()
                .map(act -> Action.builder()
                        .sensorId(act.getSensorId())
                        .type(ActionType.valueOf(act.getType().name()))
                        .value(act.getValue())
                        .build())
                .collect(Collectors.toList());
        actionRepository.saveAll(actions);

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .conditions(conditions)
                .actions(actions)
                .build();
        scenarioRepository.save(scenario);

        log.info("Scenario added: hubId={}, name={}", hubId, event.getName());
    }
}