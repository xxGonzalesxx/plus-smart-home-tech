package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.broker.CollectorTopics;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var climateEvent = event.getClimateSensor();
        var timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        var avroEvent = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp.toEpochMilli())
                .setPayload(ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateEvent.getTemperatureC())
                        .setHumidity(climateEvent.getHumidity())
                        .setCo2Level(climateEvent.getCo2Level())
                        .build())
                .build();

        kafkaTemplate.send(CollectorTopics.TELEMETRY_SENSORS_V1, avroEvent.getHubId(), avroEvent);
        log.debug("Sent climate sensor event: id={}", event.getId());
    }
}