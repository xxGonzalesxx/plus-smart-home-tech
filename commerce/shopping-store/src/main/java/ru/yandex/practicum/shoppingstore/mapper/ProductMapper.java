package ru.yandex.practicum.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.api.shoppingstore.dto.ProductDto;
import ru.yandex.practicum.shoppingstore.enums.ProductCategory;
import ru.yandex.practicum.shoppingstore.enums.ProductState;
import ru.yandex.practicum.shoppingstore.enums.QuantityState;
import ru.yandex.practicum.shoppingstore.model.Product;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(product.getQuantityState().name())
                .productState(product.getState().name())
                .productCategory(product.getCategory().name())
                .price(product.getPrice())
                .build();
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .id(dto.getProductId())
                .name(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .category(ProductCategory.valueOf(dto.getProductCategory()))
                .state(ProductState.valueOf(dto.getProductState()))
                .quantityState(QuantityState.valueOf(dto.getQuantityState()))
                .price(dto.getPrice())
                .build();
    }
}