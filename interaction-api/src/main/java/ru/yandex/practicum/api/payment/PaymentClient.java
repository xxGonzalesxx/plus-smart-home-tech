package ru.yandex.practicum.api.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.api.order.dto.OrderDto;

import java.math.BigDecimal;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody OrderDto order);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto order,@RequestParam BigDecimal deliveryCost);
}