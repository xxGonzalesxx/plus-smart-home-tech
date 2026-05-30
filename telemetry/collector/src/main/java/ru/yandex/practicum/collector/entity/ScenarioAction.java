package ru.yandex.practicum.collector.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.collector.dto.hub.model.ActionType;

@Entity
@Table(name = "scenario_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioAction {

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
    private ActionType type;  // ACTIVATE, DEACTIVATE, INVERSE, SET_VALUE

    @Column
    private Integer value;
}
