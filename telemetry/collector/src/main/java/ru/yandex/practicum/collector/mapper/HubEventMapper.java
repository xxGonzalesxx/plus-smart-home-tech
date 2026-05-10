package ru.yandex.practicum.collector.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.collector.dto.hub.model.DeviceAction;
import ru.yandex.practicum.collector.dto.hub.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.*;

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
                    .setType(DeviceTypeAvro.valueOf(added.getDeviceType()))
                    .build());
        } else if (event instanceof DeviceRemovedEvent) {
            DeviceRemovedEvent removed = (DeviceRemovedEvent) event;
            builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                    .setId(removed.getId())
                    .build());
        } else if (event instanceof ScenarioAddedEvent) {
            ScenarioAddedEvent scenario = (ScenarioAddedEvent) event;

            // Конвертируем условия
            var conditions = scenario.getConditions().stream()
                    .map(HubEventMapper::toConditionAvro)
                    .collect(Collectors.toList());

            // Конвертируем действия
            var actions = scenario.getActions().stream()
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
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation()));

        // Устанавливаем value (может быть int или null)
        if (condition.getValue() != null) {
            builder.setValue(condition.getValue());
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }

    private static DeviceActionAvro toActionAvro(DeviceAction action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }
}