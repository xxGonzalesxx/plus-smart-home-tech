package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "telemetry.sensors.v1";

    // Для REST запросов
    public void send(SensorEvent event) {
        try {
            SensorEventAvro avroEvent = SensorEventMapper.toAvro(event);
            byte[] data = serializeAvro(avroEvent);
            kafkaTemplate.send(TOPIC, event.getHubId(), data);
            log.debug("Sent sensor event to Kafka: hubId={}", event.getHubId());
        } catch (IOException e) {
            log.error("Failed to serialize sensor event to Avro", e);
        }
    }

    // Для gRPC запросов
    public void processSensorEvent(SensorEventProto protoEvent) {
        try {
            SensorEventAvro avroEvent = convertProtoToAvro(protoEvent);  // ← SensorEventAvro, не HubEventAvro
            byte[] data = serializeAvro(avroEvent);
            kafkaTemplate.send(TOPIC, protoEvent.getHubId(), data);
            log.info("Sent sensor event from gRPC to Kafka: id={}", protoEvent.getId());
        } catch (IOException e) {
            log.error("Failed to process sensor event from gRPC", e);
        }
    }

    private SensorEventAvro convertProtoToAvro(SensorEventProto protoEvent) {
        // TODO: реализовать преобразование Protobuf → Avro
        // Пример для MotionSensorEvent:
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(protoEvent.getId())
                .setHubId(protoEvent.getHubId())
                .setTimestamp(protoEvent.getTimestamp().getSeconds() * 1000);

        // Определяем тип payload и заполняем
        if (protoEvent.hasMotionSensor()) {
            var motion = protoEvent.getMotionSensor();
            builder.setPayload(ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro.newBuilder()
                    .setLinkQuality(motion.getLinkQuality())
                    .setMotion(motion.getMotion())
                    .setVoltage(motion.getVoltage())
                    .build());
        } else if (protoEvent.hasTemperatureSensor()) {
            var temp = protoEvent.getTemperatureSensor();
            builder.setPayload(ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(temp.getTemperatureC())
                    .setTemperatureF(temp.getTemperatureF())
                    .build());
        }
        // TODO: добавить остальные типы (Light, Climate, Switch)

        return builder.build();
    }

    private byte[] serializeAvro(SensorEventAvro event) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        DatumWriter<SensorEventAvro> writer = new SpecificDatumWriter<>(SensorEventAvro.getClassSchema());
        writer.write(event, encoder);
        encoder.flush();
        return outputStream.toByteArray();
    }
}