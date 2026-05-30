package ru.yandex.practicum.analyzer;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.analyzer.processor.SnapshotProcessor;
;

@Component
@AllArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final HubEventProcessor hubEventProcessor;
    final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String... args) throws Exception {

        // запускаем в отдельном потоке обработчик событий
        // от пользовательских хабов
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        snapshotProcessor.start();
    }

}