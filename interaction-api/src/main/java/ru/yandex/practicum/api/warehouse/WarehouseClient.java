package ru.yandex.practicum.api.warehouse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.api.shoppingcart.dto.ShoppingCartDto;
import ru.yandex.practicum.api.warehouse.dto.BookedProductsDto;


@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient {

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnough(@RequestBody ShoppingCartDto cart);
}