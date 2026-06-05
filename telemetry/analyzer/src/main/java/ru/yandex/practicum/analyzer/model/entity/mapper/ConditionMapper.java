package ru.yandex.practicum.analyzer.model.entity.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
public class ConditionMapper {

    public Condition fromAvro(ScenarioConditionAvro conditionAvro) {
        Condition result = new Condition();
        result.setId(null);
        result.setType(conditionAvro.getType().toString());
        result.setOperation(conditionAvro.getOperation().toString());
        result.setValue(getValue(conditionAvro.getValue()));

        return result;
    }

    private Integer getValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        return null;
    }

}