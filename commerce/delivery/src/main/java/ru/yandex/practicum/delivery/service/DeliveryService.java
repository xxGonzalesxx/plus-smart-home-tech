package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.enums.DeliveryState;
import ru.yandex.practicum.delivery.dto.AddressDto;
import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Address from = toAddress(deliveryDto.getFromAddress());
        Address to = toAddress(deliveryDto.getToAddress());

        Delivery delivery = Delivery.builder()
                .orderId(deliveryDto.getOrderId())
                .fromAddress(from)
                .toAddress(to)
                .state(DeliveryState.CREATED)
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Created delivery: {}", saved.getId());

        return toDto(saved);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);
        log.info("Delivery picked for order: {}", orderId);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        log.info("Delivery successful for order: {}", orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        log.info("Delivery failed for order: {}", orderId);
    }

    public BigDecimal deliveryCost(UUID orderId) {
        Delivery delivery = getDeliveryByOrderId(orderId);

        // Базовая стоимость
        double cost = 5.0;

        // 1. Адрес склада (fromAddress)
        String fromStreet = delivery.getFromAddress().getStreet();
        if (fromStreet != null && fromStreet.contains("ADDRESS_2")) {
            cost = (5 * 2) + 5; // 15
        } else {
            cost = (5 * 1) + 5; // 10
        }

        // 2. Хрупкость (заглушка — будет передаваться позже)
        // cost += cost * 0.2

        // 3. Вес (заглушка)
        // cost += weight * 0.3

        // 4. Объём (заглушка)
        // cost += volume * 0.2

        // 5. Адрес доставки
        String toStreet = delivery.getToAddress().getStreet();
        if (toStreet != null && !toStreet.equals(fromStreet)) {
            cost += cost * 0.2;
        }

        return BigDecimal.valueOf(cost).setScale(2, RoundingMode.HALF_UP);
    }

    private Delivery getDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
    }

    private Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .house(dto.getHouse())
                .flat(dto.getFlat())
                .build();
    }

    private AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    private DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .fromAddress(toAddressDto(delivery.getFromAddress()))
                .toAddress(toAddressDto(delivery.getToAddress()))
                .deliveryState(delivery.getState())
                .build();
    }
}