package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.dto.hub.HubEvent;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "telemetry.hubs.v1";

    // Для REST запросов (уже есть)
    public void send(HubEvent event) {
        try {
            HubEventAvro avroEvent = HubEventMapper.toAvro(event);
            byte[] data = serializeAvro(avroEvent);
            kafkaTemplate.send(TOPIC, event.getHubId(), data);
            log.debug("Sent hub event to Kafka: hubId={}", event.getHubId());
        } catch (IOException e) {
            log.error("Failed to serialize hub event to Avro", e);
        }
    }

    // Для gRPC запросов (НОВЫЙ)
    public void processHubEvent(HubEventProto protoEvent) {
        try {
            // TODO: конвертировать Protobuf → Avro
            HubEventAvro avroEvent = convertProtoToAvro(protoEvent);

            byte[] data = serializeAvro(avroEvent);
            kafkaTemplate.send(TOPIC, protoEvent.getHubId(), data);
            log.info("Sent hub event from gRPC to Kafka: hubId={}", protoEvent.getHubId());
        } catch (IOException e) {
            log.error("Failed to process hub event from gRPC", e);
        }
    }

    private HubEventAvro convertProtoToAvro(HubEventProto protoEvent) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(protoEvent.getHubId())
                .setTimestamp(protoEvent.getTimestamp().getSeconds() * 1000 +
                        protoEvent.getTimestamp().getNanos() / 1_000_000);

        // Определяем тип события и заполняем payload
        if (protoEvent.hasDeviceAdded()) {
            var deviceAdded = protoEvent.getDeviceAdded();
            builder.setPayload(DeviceAddedEventAvro.newBuilder()
                    .setId(deviceAdded.getId())
                    .setType(DeviceTypeAvro.valueOf(deviceAdded.getType().name()))
                    .build());
        } else if (protoEvent.hasDeviceRemoved()) {
            var deviceRemoved = protoEvent.getDeviceRemoved();
            builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                    .setId(deviceRemoved.getId())
                    .build());
        } else if (protoEvent.hasScenarioAdded()) {
            var scenarioAdded = protoEvent.getScenarioAdded();

            // Конвертируем условия
            var conditions = scenarioAdded.getConditionList().stream()
                    .map(condition -> ScenarioConditionAvro.newBuilder()
                            .setSensorId(condition.getSensorId())
                            .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                            .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                            .setValue(condition.hasIntValue() ? condition.getIntValue() : null)
                            .build())
                    .collect(Collectors.toList());

            // Конвертируем действия
            var actions = scenarioAdded.getActionList().stream()
                    .map(action -> DeviceActionAvro.newBuilder()
                            .setSensorId(action.getSensorId())
                            .setType(ActionTypeAvro.valueOf(action.getType().name()))
                            .setValue(action.getValue())
                            .build())
                    .collect(Collectors.toList());

            builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                    .setName(scenarioAdded.getName())
                    .setConditions(conditions)
                    .setActions(actions)
                    .build());
        } else if (protoEvent.hasScenarioRemoved()) {
            var scenarioRemoved = protoEvent.getScenarioRemoved();
            builder.setPayload(ScenarioRemovedEventAvro.newBuilder()
                    .setName(scenarioRemoved.getName())
                    .build());
        } else {
            log.error("Unknown hub event type: no payload set");
            throw new IllegalArgumentException("Unknown hub event type");
        }

        return builder.build();
    }

    private byte[] serializeAvro(HubEventAvro event) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        DatumWriter<HubEventAvro> writer = new SpecificDatumWriter<>(HubEventAvro.getClassSchema());
        writer.write(event, encoder);
        encoder.flush();
        return outputStream.toByteArray();
    }
}