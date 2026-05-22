package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.broker.CollectorTopics;
import ru.yandex.practicum.collector.mapper.grpc.DeviceTypeMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var deviceAddedEvent = event.getDeviceAdded();
        var timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        var avroEvent = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp.toEpochMilli())
                .setPayload(DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEvent.getId())
                        .setType(DeviceTypeMapper.toAvro(deviceAddedEvent.getType()))
                        .build())
                .build();

        kafkaTemplate.send(CollectorTopics.TELEMETRY_HUBS_V1, avroEvent.getHubId(), avroEvent);
        log.debug("Sent device added event: deviceId={}", deviceAddedEvent.getId());
    }
}