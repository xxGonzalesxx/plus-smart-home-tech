package ru.yandex.practicum.api.warehouse.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DimensionDto {
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
}