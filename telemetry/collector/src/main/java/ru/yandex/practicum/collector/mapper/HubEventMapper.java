package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.collector.dto.hub.model.DeviceAction;
import ru.yandex.practicum.collector.dto.hub.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class HubEventMapper {

    public static HubEventAvro toAvro(HubEvent event) {
        if (event == null) {
            log.error("❌ HubEventMapper.toAvro() received null event");
            return null;
        }

        try {
            HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp().toEpochMilli());

            if (event instanceof DeviceAddedEvent) {
                DeviceAddedEvent added = (DeviceAddedEvent) event;
                builder.setPayload(DeviceAddedEventAvro.newBuilder()
                        .setId(added.getId())
                        .setType(DeviceTypeAvro.valueOf(added.getDeviceType().name()))
                        .build());
                        
            } else if (event instanceof DeviceRemovedEvent) {
                DeviceRemovedEvent removed = (DeviceRemovedEvent) event;
                builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                        .setId(removed.getId())
                        .build());
                        
            } else if (event instanceof ScenarioAddedEvent) {
                ScenarioAddedEvent scenario = (ScenarioAddedEvent) event;

                // Null-safe check for conditions
                List<ScenarioConditionAvro> conditions;
                if (scenario.getConditions() == null) {
                    log.warn("⚠️ SCENARIO_ADDED.conditions is null for scenario: {}", scenario.getName());
                    conditions = Collections.emptyList();
                } else if (scenario.getConditions().isEmpty()) {
                    log.debug("ℹ️ SCENARIO_ADDED.conditions is empty for scenario: {}", scenario.getName());
                    conditions = Collections.emptyList();
                } else {
                    conditions = scenario.getConditions().stream()
                            .map(HubEventMapper::toConditionAvro)
                            .collect(Collectors.toList());
                    log.debug("✓ Mapped {} conditions for scenario: {}", conditions.size(), scenario.getName());
                }

                // Null-safe check for actions
                List<DeviceActionAvro> actions;
                if (scenario.getActions() == null) {
                    log.warn("⚠️ SCENARIO_ADDED.actions is null for scenario: {}", scenario.getName());
                    actions = Collections.emptyList();
                } else if (scenario.getActions().isEmpty()) {
                    log.debug("ℹ️ SCENARIO_ADDED.actions is empty for scenario: {}", scenario.getName());
                    actions = Collections.emptyList();
                } else {
                    actions = scenario.getActions().stream()
                            .map(HubEventMapper::toActionAvro)
                            .collect(Collectors.toList());
                    log.debug("✓ Mapped {} actions for scenario: {}", actions.size(), scenario.getName());
                }

                log.debug("📊 SCENARIO_ADDED mapping: name={}, conditions={}, actions={}", 
                        scenario.getName(), conditions.size(), actions.size());

                builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                        .setName(scenario.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build());
                        
            } else if (event instanceof ScenarioRemovedEvent) {
                ScenarioRemovedEvent removed = (ScenarioRemovedEvent) event;
                builder.setPayload(ScenarioRemovedEventAvro.newBuilder()
                        .setName(removed.getName())
                        .build());
            } else {
                log.warn("⚠️ Unknown event type: {}", event.getClass().getSimpleName());
            }

            return builder.build();
            
        } catch (NullPointerException e) {
            log.error("💥 NullPointerException during AVRO mapping for event type {}: {}", 
                    event.getType(), e.getMessage(), e);
            return null;
        } catch (IllegalArgumentException e) {
            log.error("💥 IllegalArgumentException during AVRO mapping for event type {}: {}", 
                    event.getType(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("💥 Unexpected exception during AVRO mapping for event type {}: {}", 
                    event.getType(), e.getMessage(), e);
            return null;
        }
    }

    private static ScenarioConditionAvro toConditionAvro(ScenarioCondition condition) {
        if (condition == null) {
            log.warn("⚠️ ScenarioCondition is null");
            return null;
        }
        
        try {
            ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                    .setSensorId(condition.getSensorId())
                    .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                    .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                    .setValue(condition.getValue());

            return builder.build();
        } catch (Exception e) {
            log.error("💥 Failed to map ScenarioCondition: {}", e.getMessage(), e);
            return null;
        }
    }

    private static DeviceActionAvro toActionAvro(DeviceAction action) {
        if (action == null) {
            log.warn("⚠️ DeviceAction is null");
            return null;
        }
        
        try {
            return DeviceActionAvro.newBuilder()
                    .setSensorId(action.getSensorId())
                    .setType(ActionTypeAvro.valueOf(action.getType().name()))
                    .setValue(action.getValue())
                    .build();
        } catch (Exception e) {
            log.error("💥 Failed to map DeviceAction: {}", e.getMessage(), e);
            return null;
        }
    }
}
