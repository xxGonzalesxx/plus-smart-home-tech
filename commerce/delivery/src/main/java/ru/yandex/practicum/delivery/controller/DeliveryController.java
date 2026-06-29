package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.delivery.dto.DeliveryDto;
import ru.yandex.practicum.api.order.dto.OrderDto;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PutMapping
    public DeliveryDto planDelivery(@RequestBody DeliveryDto deliveryDto) {
        log.info("PUT /api/v1/delivery - orderId: {}", deliveryDto.getOrderId());
        return deliveryService.planDelivery(deliveryDto);
    }

    @PostMapping("/picked")
    public void deliveryPicked(@RequestBody UUID orderId) {
        log.info("POST /api/v1/delivery/picked - orderId: {}", orderId);
        deliveryService.deliveryPicked(orderId);
    }

    @PostMapping("/successful")
    public void deliverySuccessful(@RequestBody UUID orderId) {
        log.info("POST /api/v1/delivery/successful - orderId: {}", orderId);
        deliveryService.deliverySuccessful(orderId);
    }

    @PostMapping("/failed")
    public void deliveryFailed(@RequestBody UUID orderId) {
        log.info("POST /api/v1/delivery/failed - orderId: {}", orderId);
        deliveryService.deliveryFailed(orderId);
    }

    @PostMapping("/cost")
    public BigDecimal deliveryCost(@RequestBody OrderDto order) {
        log.info("POST /api/v1/delivery/cost - orderId: {}", order.getOrderId());
        return deliveryService.deliveryCost(order);
    }
}