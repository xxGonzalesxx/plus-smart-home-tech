package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.analyzer.model.enums.ActionType;

@Entity
@Table(name = "actions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;

    private Integer value;
}