package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sensor {

    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;

}