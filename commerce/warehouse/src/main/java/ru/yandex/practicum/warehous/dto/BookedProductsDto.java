package ru.yandex.practicum.warehous.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookedProductsDto {
    private BigDecimal deliveryWeight;
    private BigDecimal deliveryVolume;
    private boolean fragile;
}