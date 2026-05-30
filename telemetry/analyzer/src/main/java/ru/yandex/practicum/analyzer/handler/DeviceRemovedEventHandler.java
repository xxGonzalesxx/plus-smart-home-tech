package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceRemovedEventHandler implements HubEventHandler<DeviceRemovedEventAvro> {
    private final SensorRepository sensorRepository;

    @Override
    public Class<DeviceRemovedEventAvro> getPayloadType() {
        return DeviceRemovedEventAvro.class;
    }

    @Override
    public void handle(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.findById(event.getId())
                .ifPresent(sensor -> {
                    sensorRepository.delete(sensor);
                    log.info("Device removed: id={}, hubId={}", event.getId(), hubId);
                });
    }
}