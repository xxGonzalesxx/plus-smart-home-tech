package ru.yandex.practicum.warehous.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "fragile")
    private boolean fragile;

    @Embedded
    private Dimension dimension;

    @Column(name = "weight")
    private double weight;
}