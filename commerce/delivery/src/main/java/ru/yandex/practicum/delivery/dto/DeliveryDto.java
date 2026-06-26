package ru.yandex.practicum.delivery.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.delivery.enums.DeliveryState;

import java.util.UUID;

@Data
@Builder
public class DeliveryDto {
    private UUID deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;
}