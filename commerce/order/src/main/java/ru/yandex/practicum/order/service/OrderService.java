package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.delivery.DeliveryClient;
import ru.yandex.practicum.api.payment.PaymentClient;
import ru.yandex.practicum.api.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.order.dto.ProductReturnRequest;
import ru.yandex.practicum.order.enums.OrderState;
import ru.yandex.practicum.order.exception.OrderNotFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        Order order = orderMapper.toEntity(request, username);
        Order saved = orderRepository.save(order);
        log.info("Created order for user: {}, id: {}", username, saved.getId());
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLED);
        orderRepository.save(order);
        log.info("Order assembled: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);
        log.info("Order assembly failed: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAID);
        orderRepository.save(order);
        log.info("Order paid: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);
        log.info("Order payment failed: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);
        log.info("Order delivered: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
        log.info("Order delivery failed: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        orderRepository.save(order);
        log.info("Order completed: {}", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrderById(orderId);

        OrderDto orderDto = orderMapper.toDto(order);
        BigDecimal productPrice = paymentClient.productCost(orderDto);
        order.setProductPrice(productPrice);

        BigDecimal deliveryPrice = deliveryClient.deliveryCost(orderId);
        order.setDeliveryPrice(deliveryPrice);

        // Рассчитываем общую стоимость
        BigDecimal total = productPrice.add(deliveryPrice);
        order.setTotalPrice(total);

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrderById(orderId);

        BigDecimal deliveryPrice = deliveryClient.deliveryCost(orderId);
        order.setDeliveryPrice(deliveryPrice);

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = findOrderById(request.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);

        for (Map.Entry<UUID, Integer> entry : request.getProducts().entrySet()) {
            order.getItems().stream()
                    .filter(item -> item.getProductId().equals(entry.getKey()))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(item.getQuantity() - entry.getValue()));
        }

        orderRepository.save(order);
        log.info("Product returned for order: {}", request.getOrderId());
        return orderMapper.toDto(order);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
}