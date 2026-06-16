package ru.yandex.practicum.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NewProductInWarehouseRequest {
    private UUID productId;
    private boolean fragile;
    private DimensionDto dimension;
    private double weight;
}