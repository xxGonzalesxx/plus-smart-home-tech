package ru.yandex.practicum.order.dto;

import lombok.Data;
import ru.yandex.practicum.api.common.dto.AddressDto;
import ru.yandex.practicum.api.shoppingcart.dto.ShoppingCartDto;

@Data
public class CreateNewOrderRequest {
    private ShoppingCartDto shoppingCart;
    private AddressDto deliveryAddress;
}