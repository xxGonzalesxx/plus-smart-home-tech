package ru.yandex.practicum.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.shoppingstore.dto.ProductDto;
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
                .quantityState(product.getQuantityState())
                .productState(product.getState())
                .productCategory(product.getCategory())
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
                .category(dto.getProductCategory())
                .state(dto.getProductState())
                .quantityState(dto.getQuantityState())
                .price(dto.getPrice())
                .build();
    }
}