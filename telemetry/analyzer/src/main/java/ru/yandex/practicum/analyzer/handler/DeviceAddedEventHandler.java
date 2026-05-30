package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Sensor;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAddedEventHandler implements HubEventHandler<DeviceAddedEventAvro> {
    private final SensorRepository sensorRepository;

    @Override
    public Class<DeviceAddedEventAvro> getPayloadType() {
        return DeviceAddedEventAvro.class;
    }

    @Override
    public void handle(String hubId, DeviceAddedEventAvro event) {
        if (sensorRepository.existsById(event.getId())) {
            log.info("Device {} already registered in hub {}", event.getId(), hubId);
            return;
        }

        Sensor sensor = Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();
        sensorRepository.save(sensor);
        log.info("Device added: id={}, hubId={}", event.getId(), hubId);
    }
}