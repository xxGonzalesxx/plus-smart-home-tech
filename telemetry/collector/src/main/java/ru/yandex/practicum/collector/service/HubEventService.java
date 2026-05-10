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
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "telemetry.hubs.v1";

    public void send(HubEvent event) {
        try {
            HubEventAvro avroEvent = HubEventMapper.toAvro(event);
            byte[] data = serializeAvro(avroEvent);  // ← ИСПОЛЬЗУЙ ЭТОТ МЕТОД
            kafkaTemplate.send(TOPIC, event.getHubId(), data);
            log.debug("Sent hub event to Kafka: hubId={}", event.getHubId());
        } catch (IOException e) {
            log.error("Failed to serialize hub event to Avro", e);
        }
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