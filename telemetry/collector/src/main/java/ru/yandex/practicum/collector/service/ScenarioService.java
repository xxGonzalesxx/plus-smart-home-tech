package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.collector.dto.hub.ScenarioAddedEvent;
import ru.yandex.practicum.collector.dto.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.collector.dto.hub.model.ScenarioCondition;
import ru.yandex.practicum.collector.dto.hub.model.DeviceAction;
import ru.yandex.practicum.collector.entity.Scenario;
import ru.yandex.practicum.collector.entity.ScenarioCondition as DbScenarioCondition;
import ru.yandex.practicum.collector.entity.ScenarioAction as DbScenarioAction;
import ru.yandex.practicum.collector.repository.ScenarioRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    /**
     * Сохранить или обновить сценарий из события Kafka
     */
    @Transactional
    public void handleScenarioAdded(String hubId, ScenarioAddedEvent event) {
        log.info("📝 Handling SCENARIO_ADDED event: name={}, hubId={}", event.getName(), hubId);

        try {
            // Проверяем, существует ли уже такой сценарий
            Scenario scenario = scenarioRepository
                    .findByNameAndHubId(event.getName(), hubId)
                    .orElse(Scenario.builder()
                            .hubId(hubId)
                            .name(event.getName())
                            .build());

            // Очищаем старые условия и действия
            scenario.getConditions().clear();
            scenario.getActions().clear();

            // Добавляем условия
            if (event.getConditions() != null && !event.getConditions().isEmpty()) {
                for (ScenarioCondition dtoCondition : event.getConditions()) {
                    DbScenarioCondition dbCondition = DbScenarioCondition.builder()
                            .sensorId(dtoCondition.getSensorId())
                            .type(dtoCondition.getType())
                            .operation(dtoCondition.getOperation())
                            .value(dtoCondition.getValue())
                            .scenario(scenario)
                            .build();
                    scenario.getConditions().add(dbCondition);
                }
                log.debug("✓ Added {} conditions to scenario", scenario.getConditions().size());
            }

            // Добавляем действия
            if (event.getActions() != null && !event.getActions().isEmpty()) {
                for (DeviceAction dtoAction : event.getActions()) {
                    DbScenarioAction dbAction = DbScenarioAction.builder()
                            .sensorId(dtoAction.getSensorId())
                            .type(dtoAction.getType())
                            .value(dtoAction.getValue())
                            .scenario(scenario)
                            .build();
                    scenario.getActions().add(dbAction);
                }
                log.debug("✓ Added {} actions to scenario", scenario.getActions().size());
            }

            scenario.setEnabled(true);
            Scenario saved = scenarioRepository.save(scenario);
            log.info("✅ Scenario saved: id={}, name={}, conditions={}, actions={}",
                    saved.getId(), saved.getName(), saved.getConditions().size(), saved.getActions().size());

        } catch (Exception e) {
            log.error("❌ Error handling SCENARIO_ADDED event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save scenario: " + event.getName(), e);
        }
    }

    /**
     * Удалить сценарий
     */
    @Transactional
    public void handleScenarioRemoved(String hubId, ScenarioRemovedEvent event) {
        log.info("🗑️ Handling SCENARIO_REMOVED event: name={}, hubId={}", event.getName(), hubId);

        try {
            scenarioRepository.findByNameAndHubId(event.getName(), hubId)
                    .ifPresentOrElse(
                            scenario -> {
                                scenarioRepository.delete(scenario);
                                log.info("✅ Scenario deleted: id={}, name={}", scenario.getId(), scenario.getName());
                            },
                            () -> log.warn("⚠️ Scenario not found: name={}, hubId={}", event.getName(), hubId)
                    );
        } catch (Exception e) {
            log.error("❌ Error handling SCENARIO_REMOVED event: {}", e.getMessage(), e);
        }
    }

    /**
     * Получить все активные сценарии для хаба
     */
    @Transactional(readOnly = true)
    public List<Scenario> getActiveScenarios(String hubId) {
        return scenarioRepository.findActiveByHubId(hubId);
    }

    /**
     * Получить сценарий по ID
     */
    @Transactional(readOnly = true)
    public Scenario getScenarioById(Long scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found: " + scenarioId));
    }
}
