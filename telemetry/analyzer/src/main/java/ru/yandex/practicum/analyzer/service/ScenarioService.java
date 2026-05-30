package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ConditionChecker conditionChecker;
    private final ActionExecutor actionExecutor;

    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();

        // Загружаем все сценарии для этого хаба
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.debug("No scenarios found for hub: {}", hubId);
            return;
        }

        log.info("Processing {} scenarios for hub: {}", scenarios.size(), hubId);

        // Проверяем каждый сценарий
        for (Scenario scenario : scenarios) {
            if (checkScenarioConditions(scenario, snapshot)) {
                log.info("Scenario '{}' activated for hub: {}", scenario.getName(), hubId);
                executeScenarioActions(scenario, snapshot);
            }
        }
    }

    private boolean checkScenarioConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        List<Condition> conditions = scenario.getConditions();

        if (conditions.isEmpty()) {
            log.warn("Scenario '{}' has no conditions", scenario.getName());
            return false;
        }

        // Все условия должны выполниться
        return conditions.stream()
                .allMatch(condition -> conditionChecker.checkCondition(condition, snapshot));
    }

    private void executeScenarioActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        List<Action> actions = scenario.getActions();

        if (actions.isEmpty()) {
            log.warn("Scenario '{}' has no actions", scenario.getName());
            return;
        }

        actions.forEach(action ->
                actionExecutor.executeAction(scenario, action, snapshot.getHubId())
        );
    }
}