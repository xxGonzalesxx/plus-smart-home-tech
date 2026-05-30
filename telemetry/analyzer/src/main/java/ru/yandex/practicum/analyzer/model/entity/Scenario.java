package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hubId;
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScenarioAction> actions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScenarioCondition> conditions = new ArrayList<>();

    // вспомогательные методы
    public void addAction(ScenarioAction action) {
        actions.add(action);
        action.setScenario(this);
    }

    public void addCondition(ScenarioCondition condition) {
        conditions.add(condition);
        condition.setScenario(this);
    }

    public void setConditions(List<ScenarioCondition> conditions) {
        this.conditions.clear();
        if (conditions != null) {
            this.conditions.addAll(conditions);
            conditions.forEach(c -> c.setScenario(this));
        }
    }

    public void setActions(List<ScenarioAction> actions) {
        this.actions.clear();
        if (actions != null) {
            this.actions.addAll(actions);
            actions.forEach(a -> a.setScenario(this));
        }
    }
}