package ru.yandex.practicum.shoppingstore.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.shoppingstore.enums.ProductCategory;
import ru.yandex.practicum.shoppingstore.enums.ProductState;
import ru.yandex.practicum.shoppingstore.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductDto {
    private UUID productId;
    private String productName;
    private String description;
    private String imageSrc;
    private QuantityState quantityState;
    private ProductState productState;
    private ProductCategory productCategory;
    private BigDecimal price;
}
