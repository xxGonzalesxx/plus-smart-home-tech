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
        if (sensorRepository.existsByIdInAndHubId(List.of(event.getId()), hubId)) {
            log.info("Попытка добавления нового устройства. " +
                    "Устройство с ID: {} уже зарегистрировано в хабе с ID: {}.", event.getId(), hubId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(event.getId());
        sensor.setHubId(hubId);

        sensorRepository.save(sensor);
        log.info("В хабе с ID: {} зарегистрировано новое устройство с ID: {}.", hubId, event.getId());
    }

}