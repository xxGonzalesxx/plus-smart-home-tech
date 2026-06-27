package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.order.dto.OrderDto;
import ru.yandex.practicum.api.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/productCost")
    public BigDecimal productCost(@RequestBody OrderDto order) {
        log.info("POST /api/v1/payment/productCost - orderId: {}", order.getOrderId());
        return paymentService.productCost(order.getOrderId(), order.getProducts());
    }

    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(@RequestBody OrderDto order,
                                   @RequestParam BigDecimal deliveryCost) {
        log.info("POST /api/v1/payment/totalCost - orderId: {}, deliveryCost: {}", order.getOrderId(), deliveryCost);
        return paymentService.getTotalCost(order.getOrderId(), order.getProducts(), deliveryCost);
    }

    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto order,
                              @RequestParam BigDecimal deliveryCost) {
        log.info("POST /api/v1/payment - orderId: {}, deliveryCost: {}", order.getOrderId(), deliveryCost);
        return paymentService.payment(order.getOrderId(), order.getProducts(), deliveryCost);
    }

    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        log.info("POST /api/v1/payment/refund - paymentId: {}", paymentId);
        paymentService.paymentSuccess(paymentId);
    }

    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        log.info("POST /api/v1/payment/failed - paymentId: {}", paymentId);
        paymentService.paymentFailed(paymentId);
    }
}