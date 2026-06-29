package ru.yandex.practicum.warehous.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.common.dto.AddressDto;
import ru.yandex.practicum.api.shoppingcart.dto.ShoppingCartDto;
import ru.yandex.practicum.api.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.api.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.api.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehous.service.WarehouseService;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    public void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        log.info("PUT /api/v1/warehouse - productId: {}", request.getProductId());
        warehouseService.addNewProduct(request);
    }

    @PostMapping("/add")
    public void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        log.info("POST /api/v1/warehouse/add - productId: {}, quantity: {}",
                request.getProductId(), request.getQuantity());
        warehouseService.addProductQuantity(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto cart) {
        log.info("POST /api/v1/warehouse/check - cartId: {}", cart.getShoppingCartId());
        return warehouseService.checkProductQuantityEnough(cart);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.info("GET /api/v1/warehouse/address");
        return warehouseService.getWarehouseAddress();
    }
}