package ru.yandex.practicum.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.analyzer.processor.SnapshotProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String[] args) {
        log.info("Starting Analyzer...");

        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();
        log.info("SnapshotProcessor is ready (using Kafka listener)");
    }
}