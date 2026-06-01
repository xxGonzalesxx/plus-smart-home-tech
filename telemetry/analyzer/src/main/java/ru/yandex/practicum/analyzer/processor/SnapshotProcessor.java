package ru.yandex.practicum.analyzer.processor;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.broker.AnalyzerTopics;
import ru.yandex.practicum.analyzer.config.SnapshotConsumerConfig;
import ru.yandex.practicum.analyzer.model.entity.Action;
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

@Component
@Slf4j
public class SnapshotProcessor {
    private final SnapshotConsumerConfig consumerConfig;
    private final ScenarioRepository scenarioRepository;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    public SnapshotProcessor(
            SnapshotConsumerConfig consumerConfig,
            ScenarioRepository scenarioRepository,
            @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.consumerConfig = consumerConfig;
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    public void start() {
        try {
            Properties properties = new Properties();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    consumerConfig.getBootstrapServers());
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getSnapshotConsumer().getKeyDeserializer());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    consumerConfig.getSnapshotConsumer().getValueDeserializer());
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    consumerConfig.getSnapshotConsumer().getAutoOffsetReset());
            properties.put(ConsumerConfig.GROUP_ID_CONFIG,
                    consumerConfig.getSnapshotConsumer().getGroupId());
            this.consumer = new KafkaConsumer<>(properties);

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(List.of(AnalyzerTopics.TELEMETRY_SNAPSHOTS_V1));

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.info("Поступили данные состояния датчиков: {}", record.value());
                    SensorsSnapshotAvro snapshot = record.value();

                    List<Scenario> scenariosForHub = scenarioRepository.findByHubId(snapshot.getHubId());
                    log.debug("Найдено сценариев для хаба {}: {}", snapshot.getHubId(), scenariosForHub.size());

                    scenariosForHub.stream()
                            .filter(scenario -> {
                                boolean hasConditions = !scenario.getConditions().isEmpty();
                                if (!hasConditions) {
                                    log.debug("Сценарий {} не имеет условий, пропускаем", scenario.getName());
                                }
                                return hasConditions;
                            })
                            .filter(scenario -> {
                                boolean conditionsMatch = matchConditions(scenario.getConditions(),
                                        snapshot.getSensorsState());
                                log.debug("Сценарий {}: условия совпадают? {}", scenario.getName(), conditionsMatch);
                                return conditionsMatch;
                            })
                            .forEach(scenario -> {
                                log.info("Условия сценария '{}' выполнены для хаба {}",
                                        scenario.getName(), snapshot.getHubId());

                                Map<String, Action> actions = scenario.getActions();
                                if (actions == null || actions.isEmpty()) {
                                    log.warn("Сценарий '{}' не имеет действий для выполнения, hubId={}",
                                            scenario.getName(), snapshot.getHubId());
                                    return;
                                }

                                log.debug("Действий для выполнения: {}", actions.size());

                                actions.forEach((sensorId, action) -> {
                                    log.debug("Выполняется действие для датчика {}: тип={}, значение={}",
                                            sensorId, action.getType(), action.getValue());

                                    try {
                                        DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                                                .setSensorId(sensorId)
                                                .setType(ActionTypeProto.valueOf(action.getType().toUpperCase()))
                                                .setValue(action.getValue() != null ? action.getValue() : 0)
                                                .build();

                                        Instant instant = Instant.now();
                                        Timestamp timestampProto = Timestamp.newBuilder()
                                                .setSeconds(instant.getEpochSecond())
                                                .setNanos(instant.getNano())
                                                .build();

                                        DeviceActionRequest grpcRequest = DeviceActionRequest.newBuilder()
                                                .setHubId(snapshot.getHubId())
                                                .setScenarioName(scenario.getName())
                                                .setAction(actionProto)
                                                .setTimestamp(timestampProto)
                                                .build();

                                        log.info("Отправляю gRPC команду: hubId={}, scenario={}, sensorId={}, actionType={}",
                                                snapshot.getHubId(), scenario.getName(), sensorId, action.getType());

                                        hubRouterClient.handleDeviceAction(grpcRequest);

                                        log.info("gRPC команда успешно доставлена: hubId={}, scenario={}, sensorId={}",
                                                snapshot.getHubId(), scenario.getName(), sensorId);

                                    } catch (IllegalArgumentException e) {
                                        log.error("Неверный тип действия: hubId={}, scenario={}, sensorId={}, actionType={}, error={}",
                                                snapshot.getHubId(), scenario.getName(), sensorId, action.getType(), e.getMessage(), e);
                                    } catch (Exception e) {
                                        log.error("Не удалось отправить gRPC команду: hubId={}, scenario={}, sensorId={}, actionType={}, error={}",
                                                snapshot.getHubId(), scenario.getName(), sensorId, action.getType(), e.getMessage(), e);
                                    }
                                });
                      });
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен сигнал остановки (WakeupException)");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Сброс буферов и фиксация смещений перед закрытием...");
                if (consumer != null) {
                    consumer.commitSync();
                }
            } catch (Exception e) {
                log.error("Ошибка при финальном сбросе данных или коммите оффсетов", e);
            } finally {
                if (consumer != null) {
                    log.info("Закрываем консьюмер");
                    consumer.close();
                }
            }
        }
    }

    private boolean matchConditions(Map<String, Condition> scenarioConditions,
                                    Map<String, SensorStateAvro> sensorsState) {
        return scenarioConditions.entrySet().stream().allMatch(entry -> {
            String sensorId = entry.getKey();
            Condition condition = entry.getValue();

            SensorStateAvro sensorState = sensorsState.get(sensorId);
            if (sensorState == null || sensorState.getData() == null) {
                log.debug("Датчик {} не найден в снапшоте или его данные == null", sensorId);
                return false;
            }

            try {
                ConditionTypeAvro conditionType = ConditionTypeAvro
                        .valueOf(condition.getType().toUpperCase());
                ConditionOperationAvro operation = ConditionOperationAvro
                        .valueOf(condition.getOperation().toUpperCase());

                Object actualValue = getSensorValue(sensorState.getData(), conditionType);
                if (actualValue == null) {
                    log.debug("Не удалось извлечь значение датчика {} для типа {}", sensorId, conditionType);
                    return false;
                }

                boolean result = checkOperation(actualValue, operation, condition.getValue());
                log.debug("Проверка условия для датчика {}: {} {} {} = {}",
                        sensorId, actualValue, operation, condition.getValue(), result);
                return result;

            } catch (IllegalArgumentException e) {
                log.error("Ошибка маппинга Condition из БД в Avro Enum: sensorId={}, type={}, operation={}, error={}",
                        sensorId, condition.getType(), condition.getOperation(), e.getMessage(), e);
                return false;
            }
        });
    }

    private Object getSensorValue(Object avroUnionData, ConditionTypeAvro conditionType) {
        return switch (conditionType) {
            case TEMPERATURE -> {
                if (avroUnionData instanceof TemperatureSensorAvro t) {
                    yield t.getTemperatureC();
                }
                if (avroUnionData instanceof ClimateSensorAvro c) {
                    yield c.getTemperatureC();
                }
                yield null;
            }
            case HUMIDITY -> avroUnionData instanceof ClimateSensorAvro c ? c.getHumidity() : null;
            case CO2LEVEL -> avroUnionData instanceof ClimateSensorAvro c ? c.getCo2Level() : null;
            case LUMINOSITY -> avroUnionData instanceof LightSensorAvro l ? l.getLuminosity() : null;
            case MOTION -> avroUnionData instanceof MotionSensorAvro m ? m.getMotion() : null;
            case SWITCH -> avroUnionData instanceof SwitchSensorAvro s ? s.getState() : null;
        };
    }

    private boolean checkOperation(Object actual, ConditionOperationAvro operation, Integer expectedValue) {
        if (actual instanceof Boolean actualBool) {
            if (operation != ConditionOperationAvro.EQUALS) {
                log.warn("Для булевых условий поддерживается только операция EQUALS. Получено: {}", operation);
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
}