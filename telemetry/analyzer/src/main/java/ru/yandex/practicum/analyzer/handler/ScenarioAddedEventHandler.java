package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.model.entity.mapper.ActionMapper;
import ru.yandex.practicum.analyzer.model.entity.mapper.ConditionMapper;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {
    private final ScenarioRepository scenarioRepository;
    private final ConditionMapper conditionMapper;
    private final ActionMapper actionMapper;

    @Override
    public Class<ScenarioAddedEventAvro> getPayloadType() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    public void handle(String hubId, ScenarioAddedEventAvro event) {
        Optional<Scenario> addedScenario = scenarioRepository.findByHubIdAndName(hubId, event.getName());
        if (addedScenario.isPresent()) {
            log.info("Попытка добавления нового сценария. " +
                    "Сценарий с названием: {} уже зарегистрирован в хабе с ID: {}.", event.getName(), hubId);
            return;
        }

        Scenario scenario = new Scenario();
        scenario.setId(null);
        scenario.setHubId(hubId);
        scenario.setName(event.getName());
        scenario.setConditions(
                event.getConditions().stream()
                        .collect(Collectors.toMap(
                                        ScenarioConditionAvro::getSensorId,
                                        conditionMapper::fromAvro
                                )
                        )
        );
        scenario.setActions(
                event.getActions().stream()
                        .collect(Collectors.toMap(
                                        DeviceActionAvro::getSensorId,
                                        actionMapper::fromAvro
                                )
                        )
        );

        scenarioRepository.save(scenario);
        log.info("В хабе с ID: {} зарегистрирован новый сценарий с названием: {}.", hubId, event.getName());
    }

}