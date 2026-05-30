package ru.yandex.practicum.collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.collector.entity.Scenario;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     * Найти все активные сценарии для хаба
     */
    @Query("SELECT s FROM Scenario s WHERE s.hubId = ?1 AND s.enabled = true")
    List<Scenario> findActiveByHubId(String hubId);

    /**
     * Найти сценарий по имени и hubId
     */
    Optional<Scenario> findByNameAndHubId(String name, String hubId);

    /**
     * Найти все сценарии для хаба (включая отключённые)
     */
    List<Scenario> findByHubId(String hubId);

    /**
     * Проверить существование сценария
     */
    boolean existsByNameAndHubId(String name, String hubId);
}
