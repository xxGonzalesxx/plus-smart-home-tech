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

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

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
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Integer> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findByProductId(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                            "Product not found in warehouse: " + productId));

            if (product.getQuantity() < quantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product: " + productId +
                                ". Available: " + product.getQuantity() +
                                ", requested: " + quantity);
            }

            totalWeight += product.getWeight() * quantity;

            double volume = product.getDimension().getWidth()
                    * product.getDimension().getHeight()
                    * product.getDimension().getDepth();
            totalVolume += volume * quantity;

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