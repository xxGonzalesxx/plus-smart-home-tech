package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.broker.CollectorTopics;
import ru.yandex.practicum.collector.mapper.grpc.DeviceActionMapper;
import ru.yandex.practicum.collector.mapper.grpc.ScenarioConditionMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var scenarioEvent = event.getScenarioAdded();
        var timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        var conditions = scenarioEvent.getConditionList().stream()
                .map(ScenarioConditionMapper::toAvro)
                .toList();

        var actions = scenarioEvent.getActionList().stream()
                .map(DeviceActionMapper::toAvro)
                .toList();

        var avroEvent = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp.toEpochMilli())
                .setPayload(ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioEvent.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build())
                .build();

        kafkaTemplate.send(CollectorTopics.TELEMETRY_HUBS_V1, avroEvent.getHubId(), avroEvent);
        log.debug("Sent scenario added event: name={}", scenarioEvent.getName());
    }
}