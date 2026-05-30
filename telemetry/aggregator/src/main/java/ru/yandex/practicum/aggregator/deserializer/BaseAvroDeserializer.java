package ru.yandex.practicum.aggregator.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DecoderFactory decoderFactory;
    private final Schema schema;
    private final DatumReader<T> reader;

    protected BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    protected BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = decoderFactory.binaryDecoder(inputStream, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize Avro message from topic: " + topic, e);
        }
    }
}