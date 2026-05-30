package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import com.google.protobuf.Timestamp;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutor {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void executeAction(Scenario scenario, Action action, String hubId, String sensorId) {
        try {
            // Преобразуем ActionType (enum из entity) в ActionTypeProto
            ActionTypeProto actionType = ActionTypeProto.valueOf(action.getType().name());

            // Создаём DeviceActionProto
            DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(actionType)
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            // Создаём запрос
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenario.getName())
                    .setAction(deviceAction)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            // Отправляем команду в Hub Router
            hubRouterClient.handleDeviceAction(request);

            log.info("✅ Action executed: scenario='{}', actionType={}, sensorId={}, hubId={}",
                    scenario.getName(), action.getType(), sensorId, hubId);
        } catch (Exception e) {
            log.error("❌ Failed to execute action: scenario='{}', hubId={}, error={}",
                    scenario.getName(), hubId, e.getMessage(), e);
        }
    }
}