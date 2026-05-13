package ru.yandex.practicum.collector.dto.hub;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    private String name;
    private String type = "SCENARIO_REMOVED";

    @Override
    public String getType() {
        return type;
    }
}