package ru.yandex.practicum.api.shoppingstore.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductDto {
    private UUID productId;
    private String productName;
    private String description;
    private String imageSrc;
    private String quantityState;
    private String productState;
    private String productCategory;
    private BigDecimal price;
}