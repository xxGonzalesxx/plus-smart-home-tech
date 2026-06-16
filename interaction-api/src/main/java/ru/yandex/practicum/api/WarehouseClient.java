package ru.yandex.practicum.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.api.dto.BookedProductsDto;
import ru.yandex.practicum.api.dto.ShoppingCartDto;


@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient {

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnough(@RequestBody ShoppingCartDto cart);
}