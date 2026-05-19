package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.collector.service.SensorEventService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class SensorEventController {

    private final SensorEventService sensorEventService;

    @PostMapping("/sensors")
    public void collectSensorEvent(@RequestBody SensorEvent event) {
        log.info("Received sensor event (REST): type={}, hubId={}, id={}",
                event.getType(), event.getHubId(), event.getId());
        sensorEventService.send(event);
    }
}