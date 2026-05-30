package ru.yandex.practicum.collector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @OneToMany(
            mappedBy = "scenario",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    private List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(
            mappedBy = "scenario",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    private List<ScenarioAction> actions = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void addCondition(ScenarioCondition condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        condition.setScenario(this);
        this.conditions.add(condition);
    }

    public void addAction(ScenarioAction action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        action.setScenario(this);
        this.actions.add(action);
    }
}
