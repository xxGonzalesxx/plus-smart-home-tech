package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

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
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Scenario removed: hubId={}, name={}", hubId, event.getName());
                });
    }
}