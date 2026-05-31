package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioRemovedEventHandler implements HubEventHandler<ScenarioRemovedEventAvro> {
    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<ScenarioRemovedEventAvro> getPayloadType() {
        return ScenarioRemovedEventAvro.class;
    }

    @Override
    public void handle(String hubId, ScenarioRemovedEventAvro event) {
        Optional<Scenario> removedScenario = scenarioRepository.findByHubIdAndName(hubId, event.getName());
        if (removedScenario.isEmpty()) {
            log.info("Удаление сценария. Сценарий с названием: {} не найден в хабе с ID: {}.", event.getName(), hubId);
            return;
        }

        scenarioRepository.deleteById(removedScenario.get().getId());
        log.info("Удаление сценария с названием: {} в хабе с ID: {}.", event.getName(), hubId);
    }

}