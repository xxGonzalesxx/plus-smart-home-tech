package ru.yandex.practicum.warehous.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookedProductsDto {
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
}