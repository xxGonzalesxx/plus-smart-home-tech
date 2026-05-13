package ru.yandex.practicum.collector.dto.hub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.collector.dto.hub.model.DeviceAction;
import ru.yandex.practicum.collector.dto.hub.model.ScenarioCondition;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    private String name;
    private List<ScenarioCondition> conditions;
    private List<DeviceAction> actions;
    private String type = "SCENARIO_ADDED";

    @Override
    public String getType() {
        return type;
    }
}