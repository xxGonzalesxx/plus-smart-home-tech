package ru.yandex.practicum.order.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.api.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.order.enums.OrderState;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.model.OrderItem;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        Map<UUID, Integer> products = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));

        return OrderDto.builder()
                .orderId(order.getId())
                .shoppingCartId(order.getShoppingCartId())
                .products(products)
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState().name())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.isFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public Order toEntity(CreateNewOrderRequest request, String username) {
        Order order = Order.builder()
                .username(username)
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .state(OrderState.NEW)
                .build();

        Map<UUID, Integer> products = request.getShoppingCart().getProducts();
        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            OrderItem item = OrderItem.builder()
                    .productId(entry.getKey())
                    .quantity(entry.getValue())
                    .order(order)
                    .build();
            order.getItems().add(item);
        }

        return order;
    }
}