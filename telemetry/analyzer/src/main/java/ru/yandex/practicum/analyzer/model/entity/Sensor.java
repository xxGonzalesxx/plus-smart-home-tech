package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;
}