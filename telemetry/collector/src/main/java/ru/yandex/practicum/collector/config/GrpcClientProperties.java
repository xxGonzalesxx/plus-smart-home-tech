package ru.yandex.practicum.collector.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "grpc.client")
public class GrpcClientProperties {
    private HubRouterClient hubRouter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HubRouterClient {
        private String address = "localhost:9090";
        private boolean enableKeepAlive = true;
        private int keepAliveTime = 30;
        private int keepAliveTimeout = 5;
        private boolean keepAliveWithoutCalls = true;
    }
}
