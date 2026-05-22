package ru.yandex.practicum.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

public final class DeviceTypeMapper {

    private DeviceTypeMapper() {
    }

    public static DeviceTypeAvro toAvro(DeviceTypeProto type) {
        if (type == null) {
            return null;
        }
        return DeviceTypeAvro.valueOf(type.name());
    }
}