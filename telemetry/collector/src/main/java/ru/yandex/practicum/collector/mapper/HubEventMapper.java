package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.collector.dto.hub.model.DeviceAction;
import ru.yandex.practicum.collector.dto.hub.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Collections;
import java.util.stream.Collectors;

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

            // Null-safety для списка условий
            var conditions = (scenario.getConditions() != null ? scenario.getConditions() : Collections.<ScenarioCondition>emptyList())
                    .stream()
                    .filter(condition -> condition != null)  // фильтруем null элементы
                    .map(HubEventMapper::toConditionAvro)
                    .collect(Collectors.toList());

            // Null-safety для списка действий
            var actions = (scenario.getActions() != null ? scenario.getActions() : Collections.<DeviceAction>emptyList())
                    .stream()
                    .filter(action -> action != null)  // фильтруем null элементы
                    .map(HubEventMapper::toActionAvro)
                    .collect(Collectors.toList());

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