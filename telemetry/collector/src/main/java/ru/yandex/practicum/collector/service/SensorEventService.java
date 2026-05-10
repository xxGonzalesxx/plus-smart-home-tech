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
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "telemetry.sensors.v1";

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

    private byte[] serializeAvro(SensorEventAvro event) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        DatumWriter<SensorEventAvro> writer = new SpecificDatumWriter<>(SensorEventAvro.getClassSchema());
        writer.write(event, encoder);
        encoder.flush();
        return outputStream.toByteArray();
    }
}