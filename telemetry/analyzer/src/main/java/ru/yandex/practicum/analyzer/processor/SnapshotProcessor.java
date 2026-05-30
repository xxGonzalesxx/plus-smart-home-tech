package ru.yandex.practicum.analyzer.processor;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class SnapshotProcessor {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ScenarioRepository scenarioRepository;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    public SnapshotProcessor(
            ScenarioRepository scenarioRepository,
            @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    public void start() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.analyzer.deserializer.SensorsSnapshotDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-snapshot-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("telemetry.snapshots.v1"));

        log.info("SnapshotProcessor started, polling for snapshots...");

        try {
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    SensorsSnapshotAvro snapshot = record.value();
                    log.info("Received snapshot for hub: {}", snapshot.getHubId());

                    List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

                    scenarios.stream()
                            .filter(scenario -> !scenario.getConditions().isEmpty())
                            .filter(scenario -> matchConditions(scenario.getConditions(), snapshot.getSensorsState()))
                            .forEach(scenario -> {
                                log.info("Scenario '{}' activated for hub: {}", scenario.getName(), snapshot.getHubId());
                                executeActions(scenario, snapshot);
                            });
                });
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor stopped");
        } catch (Exception e) {
            log.error("Error in SnapshotProcessor", e);
        } finally {
            consumer.close();
            log.info("SnapshotProcessor closed");
        }
    }

    private boolean matchConditions(List<Condition> conditions, Map<String, SensorStateAvro> sensorsState) {
        return conditions.stream().allMatch(condition -> {
            SensorStateAvro sensorState = sensorsState.get(condition.getSensorId());
            if (sensorState == null) {
                return false;
            }

            Object actualValue = getSensorValue(sensorState.getData(), condition.getType());
            if (actualValue == null) {
                return false;
            }

            return checkOperation(actualValue, condition.getOperation(), condition.getValue());
        });
    }

    private Object getSensorValue(Object data, ru.yandex.practicum.analyzer.model.enums.ConditionType conditionType) {
        return switch (conditionType) {
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro t) yield t.getTemperatureC();
                if (data instanceof ClimateSensorAvro c) yield c.getTemperatureC();
                yield null;
            }
            case HUMIDITY -> data instanceof ClimateSensorAvro c ? c.getHumidity() : null;
            case CO2LEVEL -> data instanceof ClimateSensorAvro c ? c.getCo2Level() : null;
            case LUMINOSITY -> data instanceof LightSensorAvro l ? l.getLuminosity() : null;
            case MOTION -> data instanceof MotionSensorAvro m ? m.getMotion() : null;
            case SWITCH -> data instanceof SwitchSensorAvro s ? s.getState() : null;
        };
    }

    private boolean checkOperation(Object actual, ru.yandex.practicum.analyzer.model.enums.ConditionOperation operation, Integer expectedValue) {
        if (actual instanceof Boolean actualBool) {
            if (operation != ru.yandex.practicum.analyzer.model.enums.ConditionOperation.EQUALS) {
                return false;
            }
            boolean expectedBool = expectedValue != null && expectedValue != 0;
            return actualBool == expectedBool;
        }

        if (actual instanceof Integer actualInt) {
            if (expectedValue == null) return false;
            return switch (operation) {
                case EQUALS -> actualInt.equals(expectedValue);
                case GREATER_THAN -> actualInt > expectedValue;
                case LOWER_THAN -> actualInt < expectedValue;
            };
        }

        return false;
    }

    private void executeActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        scenario.getActions().forEach(action -> {
            try {
                DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ActionTypeProto.valueOf(action.getType().name()))
                        .setValue(action.getValue() != null ? action.getValue() : 0)
                        .build();

                Instant now = Instant.now();
                Timestamp timestamp = Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build();

                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(snapshot.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(actionProto)
                        .setTimestamp(timestamp)
                        .build();

                hubRouterClient.handleDeviceAction(request);
                log.info("Action executed: sensorId={}, type={}", action.getSensorId(), action.getType());
            } catch (Exception e) {
                log.error("Failed to execute action: sensorId={}", action.getSensorId(), e);
            }
        });
    }
}