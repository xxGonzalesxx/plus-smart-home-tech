package ru.yandex.practicum.collector.config;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.hub.router.grpc.HubRouterControllerGrpc;

/**
 * Конфигурация gRPC клиента для Hub Router
 *
 * Требуемые свойства в application.yml:
 * grpc:
 *   client:
 *     hub-router:
 *       address: localhost:9090
 *       enable-keep-alive: true
 *       keep-alive-time: 30
 *       keep-alive-timeout: 5
 *       keep-alive-without-calls: true
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GrpcClientProperties.class)
public class GrpcClientConfig {

    /**
     * Регистрируем gRPC клиент как bean
     * Используется аннотация @GrpcClient("hub-router") для инъекции
     */
    @Bean
    public void configureGrpcClient(GrpcClientProperties properties) {
        log.info("🔧 Configuring gRPC client for Hub Router: address={}", properties.getHubRouter().getAddress());
    }
}
