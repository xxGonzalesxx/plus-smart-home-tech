package ru.yandex.practicum.warehous.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.api.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehous.model.Dimension;
import ru.yandex.practicum.warehous.model.WarehouseProduct;


@Component
public class WarehouseMapper {

    public WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        if (request == null) {
            return null;
        }

        Dimension dimension = Dimension.builder()
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .build();

        return WarehouseProduct.builder()
                .productId(request.getProductId())
                .fragile(request.isFragile())
                .dimension(dimension)
                .weight(request.getWeight())
                .quantity(0)
                .build();
    }
}