package ru.yandex.practicum.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DimensionDto {
    private double width;
    private double height;
    private double depth;
}