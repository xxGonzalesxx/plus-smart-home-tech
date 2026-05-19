package ru.yandex.practicum.collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @PostMapping("/hubs")
    public void collectHubEvent(@RequestBody HubEvent event) {
        try {
            if (event == null) {
                log.error("❌ Received null hub event!");
                return;
            }

            String eventType = event.getType();
            String hubId = event.getHubId();
            
            log.info("📨 Received hub event: type={}, hubId={}, timestamp={}", 
                    eventType, hubId, event.getTimestamp());

            // Детальное логирование SCENARIO_ADDED для отладки
            if ("SCENARIO_ADDED".equals(eventType)) {
                try {
                    String eventJson = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(event);
                    log.debug("SCENARIO_ADDED full payload:\n{}", eventJson);
                } catch (Exception e) {
                    log.debug("Could not serialize event to JSON for logging", e);
                }
            }

            hubEventService.send(event);
        } catch (Exception e) {
            log.error("💥 Exception in HubEventController.collectHubEvent", e);
            throw new RuntimeException("Failed to process hub event", e);
        }
    }
}
