package ru.yandex.practicum.collector.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.collector.dto.hub.model.ConditionOperation;
import ru.yandex.practicum.collector.dto.hub.model.ConditionType;

@Entity
@Table(name = "scenario_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    @ToString.Exclude
    private Scenario scenario;

    @Column(nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType type;  // MOTION, LUMINOSITY, SWITCH, TEMPERATURE, CO2LEVEL, HUMIDITY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionOperation operation;  // EQUALS, GREATER_THAN, LOWER_THAN

    @Column(nullable = false)
    private Integer value;
}
