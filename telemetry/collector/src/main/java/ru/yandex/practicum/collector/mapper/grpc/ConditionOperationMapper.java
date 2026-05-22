package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;

public final class ConditionOperationMapper {

    private ConditionOperationMapper() {
    }

    public static ConditionOperationAvro toAvro(ConditionOperationProto operation) {
        if (operation == null) {
            return null;
        }
        return ConditionOperationAvro.valueOf(operation.name());
    }
}