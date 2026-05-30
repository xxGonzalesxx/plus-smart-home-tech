package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionOperation operation;

    private Integer value;
}