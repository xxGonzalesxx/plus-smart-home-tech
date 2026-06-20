package ru.yandex.practicum.warehous.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.warehous.model.WarehouseProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {
    Optional<WarehouseProduct> findByProductId(UUID productId);
    List<WarehouseProduct> findAllByProductIdIn(List<UUID> productIds);
    boolean existsByProductId(UUID productId);
}
