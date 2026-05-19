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
        if (event == null) {
            log.error("❌ HubEventService.send() received null event");
            return;
        }

        String eventType = event.getType();
        String hubId = event.getHubId();
        
        try {
            log.debug("🔄 Processing {} for hubId={}", eventType, hubId);

            // Step 1: Map to Avro
            HubEventAvro avroEvent = HubEventMapper.toAvro(event);
            if (avroEvent == null) {
                log.error("❌ HubEventMapper.toAvro() returned null for event type: {}, hubId: {}", 
                        eventType, hubId);
                return;
            }
            log.debug("✓ Successfully mapped {} to Avro", eventType);

            // Step 2: Serialize to binary
            byte[] data = serializeAvro(avroEvent);
            if (data == null || data.length == 0) {
                log.error("❌ Avro serialization returned empty data for event type: {}, hubId: {}", 
                        eventType, hubId);
                return;
            }
            log.debug("✓ Successfully serialized {} to {} bytes", eventType, data.length);

            // Step 3: Send to Kafka
            kafkaTemplate.send(TOPIC, hubId, data);
            log.info("✅ Successfully sent {} to Kafka: hubId={}, topic={}, payload_size={} bytes", 
                    eventType, hubId, TOPIC, data.length);
                    
        } catch (NullPointerException e) {
            log.error("💥 NullPointerException while processing {}: {}", eventType, e.getMessage(), e);
        } catch (IOException e) {
            log.error("💥 IOException while serializing {}: {}", eventType, e.getMessage(), e);
        } catch (Exception e) {
            log.error("💥 Unexpected exception while processing {}: {}", eventType, e.getMessage(), e);
        }
    }

    private byte[] serializeAvro(HubEventAvro event) throws IOException {
        if (event == null) {
            log.error("❌ serializeAvro() received null event");
            return new byte[0];
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            DatumWriter<HubEventAvro> writer = new SpecificDatumWriter<>(HubEventAvro.getClassSchema());
            writer.write(event, encoder);
            encoder.flush();
            
            byte[] result = outputStream.toByteArray();
            log.debug("📦 Serialized Avro event to {} bytes", result.length);
            return result;
            
        } catch (IOException e) {
            log.error("💥 Failed to serialize Avro event: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during Avro serialization: {}", e.getMessage(), e);
            throw new IOException("Failed to serialize Avro event", e);
        }
    }
}
