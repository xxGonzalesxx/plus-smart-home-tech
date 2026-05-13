package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.dto.hub.HubEvent;
import ru.yandex.practicum.collector.service.HubEventService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class HubEventController {

    private final HubEventService hubEventService;

    @PostMapping("/hubs")
    public void collectHubEvent(@RequestBody HubEvent event) {
        log.info("Received hub event: type={}, hubId={}",
                event.getType(), event.getHubId());

        hubEventService.send(event);
    }
}