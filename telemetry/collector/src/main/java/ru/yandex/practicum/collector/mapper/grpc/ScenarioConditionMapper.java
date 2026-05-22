package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

public final class ScenarioConditionMapper {

    private ScenarioConditionMapper() {
    }

    public static ScenarioConditionAvro toAvro(ScenarioConditionProto condition) {
        if (condition == null) {
            return null;
        }

        Object value = null;
        if (condition.hasIntValue()) {
            value = condition.getIntValue();
        } else if (condition.hasBoolValue()) {
            value = condition.getBoolValue();
        }

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeMapper.toAvro(condition.getType()))
                .setOperation(ConditionOperationMapper.toAvro(condition.getOperation()))
                .setValue(value)
                .build();
    }
}