package ru.yandex.practicum.shoppingcart.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Integer> products;
}