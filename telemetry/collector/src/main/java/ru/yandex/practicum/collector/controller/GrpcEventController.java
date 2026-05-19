package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.service.HubEventService;
import ru.yandex.practicum.collector.service.SensorEventService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcEventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final SensorEventService sensorEventService;
    private final HubEventService hubEventService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Received gRPC sensor event: id={}, hubId={}", request.getId(), request.getHubId());

            // ✅ ОТПРАВКА В KAFKA
            sensorEventService.processSensorEvent(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error processing sensor event", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Received gRPC hub event: hubId={}", request.getHubId());

            // ✅ ОТПРАВКА В KAFKA
            hubEventService.processHubEvent(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error processing hub event", e);
            responseObserver.onError(e);
        }
    }
}