package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.collector.dto.hub.DeviceAddedEvent;
import ru.yandex.practicum.collector.dto.hub.DeviceRemovedEvent;
import ru.yandex.practicum.collector.dto.hub.HubEvent;
import ru.yandex.practicum.collector.dto.hub.ScenarioAddedEvent;
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
        if (event == null) return null;

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

            // ✅ Проверка на null для conditions
            List<ScenarioConditionAvro> conditions;
            if (scenario.getConditions() == null) {
                log.warn("Conditions list is null for scenario: {}", scenario.getName());
                conditions = Collections.emptyList();
            } else {
                conditions = scenario.getConditions().stream()
                        .map(HubEventMapper::toConditionAvro)
                        .collect(Collectors.toList());
            }

            // ✅ Проверка на null для actions
            List<DeviceActionAvro> actions;
            if (scenario.getActions() == null) {
                log.warn("Actions list is null for scenario: {}", scenario.getName());
                actions = Collections.emptyList();
            } else {
                actions = scenario.getActions().stream()
                        .map(HubEventMapper::toActionAvro)
                        .collect(Collectors.toList());
            }

            builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                    .setName(scenario.getName())
                    .setConditions(conditions)
                    .setActions(actions)
                    .build());
        }

        return builder.build();
    }

    private static ScenarioConditionAvro toConditionAvro(ScenarioCondition condition) {
        if (condition == null) return null;

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private static DeviceActionAvro toActionAvro(DeviceAction action) {
        if (action == null) return null;

        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}