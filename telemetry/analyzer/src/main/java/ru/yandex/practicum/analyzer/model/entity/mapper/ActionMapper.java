package ru.yandex.practicum.analyzer.model.entity.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

@Component
public class ActionMapper {

    public Action fromAvro(DeviceActionAvro deviceAction) {
        Action result = new Action();
        result.setId(null);
        result.setType(deviceAction.getType().toString());
        result.setValue(getValue(deviceAction.getValue()));
        return result;
    }

    private Integer getValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        return 0;
    }
}