package ru.yandex.practicum.warehous.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dimension {

    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
}