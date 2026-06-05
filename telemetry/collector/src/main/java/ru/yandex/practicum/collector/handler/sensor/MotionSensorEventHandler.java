package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.broker.CollectorTopics;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class MotionSensorEventHandler implements SensorEventHandler {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        MotionSensorProto motionSensorEvent = event.getMotionSensor();
        Instant timestamp = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());
        SensorEventAvro avroData = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(
                        MotionSensorAvro.newBuilder()
                                .setLinkQuality(motionSensorEvent.getLinkQuality())
                                .setMotion(motionSensorEvent.getMotion())
                                .setVoltage(motionSensorEvent.getVoltage())
                                .build()
                )
                .build();

        String topic = CollectorTopics.TELEMETRY_SENSORS_V1;
        String key = avroData.getHubId();
        log.info("Готовы данные в формате Avro: >>> {} <<< для отправки в Kafka-топик: >>> {} <<<", avroData, topic);

        kafkaTemplate.send(topic, key, avroData);
    }

}