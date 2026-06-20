package ru.yandex.practicum.warehous.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class NewProductInWarehouseRequest {
    private UUID productId;
    private boolean fragile;
    private DimensionDto dimension;
    private BigDecimal weight;
}