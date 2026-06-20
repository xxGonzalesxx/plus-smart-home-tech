package ru.yandex.practicum.warehous.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.warehous.dto.*;
import ru.yandex.practicum.warehous.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.warehous.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.warehous.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.warehous.mapper.WarehouseMapper;
import ru.yandex.practicum.warehous.model.WarehouseProduct;
import ru.yandex.practicum.warehous.repository.WarehouseProductRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseMapper warehouseMapper;

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsByProductId(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Product already in warehouse: " + request.getProductId());
        }

        WarehouseProduct product = warehouseMapper.toEntity(request);
        warehouseProductRepository.save(product);
        log.info("Added new product to warehouse: {}", request.getProductId());
    }

    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseProductRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Product not found in warehouse: " + request.getProductId()));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(product);
        log.info("Added {} units to product {} in warehouse", request.getQuantity(), request.getProductId());
    }

    public BookedProductsDto checkProductQuantityEnough(ShoppingCartDto cart) {
        // 1. Получить все ID продуктов из корзины
        List<UUID> productIds = new ArrayList<>(cart.getProducts().keySet());

        // 2. ОДИН запрос к БД, вместо всех(оптимизация)
        List<WarehouseProduct> warehouseProducts = warehouseProductRepository.findAllByProductIdIn(productIds);

        // 3. Преобразовать в Map для быстрого доступа
        Map<UUID, WarehouseProduct> productMap = warehouseProducts.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        // 4. Переменные для расчётов
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean hasFragile = false;

        // 5. Проверка
        for (Map.Entry<UUID, Integer> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            WarehouseProduct product = productMap.get(productId);
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException("Product not found: " + productId);
            }

            if (product.getQuantity() < quantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product: " + productId +
                                ". Available: " + product.getQuantity() +
                                ", requested: " + quantity);
            }

            // Вес
            BigDecimal weight = product.getWeight().multiply(BigDecimal.valueOf(quantity));
            totalWeight = totalWeight.add(weight);

            // Объём
            BigDecimal volume = product.getDimension().getWidth()
                    .multiply(product.getDimension().getHeight())
                    .multiply(product.getDimension().getDepth());
            BigDecimal totalVolumeForProduct = volume.multiply(BigDecimal.valueOf(quantity));
            totalVolume = totalVolume.add(totalVolumeForProduct);

            if (product.isFragile()) {
                hasFragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();
    }

    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }
}