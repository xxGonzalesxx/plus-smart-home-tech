package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

public final class ConditionTypeMapper {

    private ConditionTypeMapper() {
    }

    public static ConditionTypeAvro toAvro(ConditionTypeProto type) {
        if (type == null) {
            return null;
        }
        return ConditionTypeAvro.valueOf(type.name());
    }
}