package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

public final class ActionTypeMapper {

    private ActionTypeMapper() {
    }

    public static ActionTypeAvro toAvro(ActionTypeProto type) {
        if (type == null) {
            return null;
        }
        return ActionTypeAvro.valueOf(type.name());
    }
}