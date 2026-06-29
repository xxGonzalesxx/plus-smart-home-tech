package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.payment.dto.PaymentDto;
import ru.yandex.practicum.api.shoppingstore.ShoppingStoreClient;
import ru.yandex.practicum.api.shoppingstore.dto.ProductDto;
import ru.yandex.practicum.payment.enums.PaymentStatus;
import ru.yandex.practicum.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;

    // 2.1 Расчёт стоимости товаров
    public BigDecimal productCost(UUID orderId, Map<UUID, Integer> products) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);
            BigDecimal price = product.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    // 2.2 Расчёт полной стоимости (товары + НДС 10% + доставка)
    public BigDecimal getTotalCost(UUID orderId, Map<UUID, Integer> products, BigDecimal deliveryCost) {
        BigDecimal productCost = productCost(orderId, products);
        BigDecimal fee = productCost.multiply(BigDecimal.valueOf(0.1));
        return productCost.add(fee).add(deliveryCost);
    }

    // 2.3 Создание оплаты
    @Transactional
    public PaymentDto payment(UUID orderId, Map<UUID, Integer> products, BigDecimal deliveryCost) {
        BigDecimal productPrice = productCost(orderId, products);
        BigDecimal fee = productPrice.multiply(BigDecimal.valueOf(0.1));
        BigDecimal totalPrice = productPrice.add(fee).add(deliveryCost);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .productPrice(productPrice)
                .deliveryPrice(deliveryCost)
                .fee(fee)
                .totalPrice(totalPrice)
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Created payment for order: {}", orderId);

        return toDto(saved);
    }

    // 2.4 Эмуляция успешной оплаты
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        log.info("Payment success: {}", paymentId);
    }

    // 2.5 Эмуляция неудачной оплаты
    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        log.info("Payment failed: {}", paymentId);
    }

    private Payment getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getId())
                .totalPayment(payment.getTotalPrice())
                .deliveryTotal(payment.getDeliveryPrice())
                .feeTotal(payment.getFee())
                .build();
    }
}