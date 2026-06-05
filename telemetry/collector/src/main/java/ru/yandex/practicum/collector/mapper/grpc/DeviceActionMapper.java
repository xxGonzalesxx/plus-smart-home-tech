package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

public final class DeviceActionMapper {

    private DeviceActionMapper() {
    }

    public static DeviceActionAvro toAvro(DeviceActionProto action) {
        if (action == null) {
            return null;
        }

        Integer value = action.hasValue() ? action.getValue() : null;

        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeMapper.toAvro(action.getType()))
                .setValue(value)
                .build();
    }
}