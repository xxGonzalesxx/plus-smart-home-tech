package ru.yandex.practicum.shoppingstore.dto;

import lombok.Data;
import ru.yandex.practicum.shoppingstore.enums.QuantityState;

import java.util.UUID;

@Data
public class SetProductQuantityStateRequest {
    private UUID productId;
    private QuantityState quantityState;
}