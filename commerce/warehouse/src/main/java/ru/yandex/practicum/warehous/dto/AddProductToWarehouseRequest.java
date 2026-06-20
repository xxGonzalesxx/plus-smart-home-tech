package ru.yandex.practicum.warehous.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddProductToWarehouseRequest {
    private UUID productId;
    private long quantity;
}