package ru.yandex.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

import java.util.List;

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
        if (!sensorRepository.existsByIdInAndHubId(List.of(event.getId()), hubId)) {
            log.info("Удаление устройства. Устройство с ID: {} не найдено в хабе с ID: {}.", event.getId(), hubId);
            return;
        }

        sensorRepository.deleteById(event.getId());
        log.info("Удаление устройства с ID: {} в хабе с ID: {}.", event.getId(), hubId);
    }

}