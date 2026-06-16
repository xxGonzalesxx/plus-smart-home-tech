package ru.yandex.practicum.shoppingcart.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ChangeProductQuantityRequest {
    private UUID productId;
    private int newQuantity;
}
