package ru.yandex.practicum.shoppingcart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.api.shoppingcart.dto.ShoppingCartDto;
import ru.yandex.practicum.api.warehouse.WarehouseClient;
import ru.yandex.practicum.api.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.shoppingcart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingcart.exception.CartNotFoundException;
import ru.yandex.practicum.shoppingcart.model.Cart;
import ru.yandex.practicum.shoppingcart.model.CartItem;
import ru.yandex.practicum.shoppingcart.repository.CartItemRepository;
import ru.yandex.practicum.shoppingcart.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartDto getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProductsToCart(String username, Map<UUID, Integer> products) {
        Cart cart = getOrCreateCart(username);
        validateCartActive(cart);

        // 1. Проверяем наличие товаров на складе
        ShoppingCartDto cartDto = new ShoppingCartDto();
        cartDto.setShoppingCartId(cart.getId());
        cartDto.setProducts(products);

        try {
            BookedProductsDto booked = warehouseClient.checkProductQuantityEnough(cartDto);
            log.info("Warehouse check passed: weight={}, volume={}, fragile={}",
                    booked.getDeliveryWeight(), booked.getDeliveryVolume(), booked.isFragile());
        } catch (Exception e) {
            log.error("Warehouse check failed: {}", e.getMessage());
            throw new RuntimeException("Not enough products in warehouse: " + e.getMessage());
        }

        // 2. Добавляем товары в корзину
        products.forEach((productId, quantity) -> {
            CartItem existingItem = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                CartItem newItem = CartItem.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .cart(cart)
                        .build();
                cart.getItems().add(newItem);
            }
        });

        Cart savedCart = cartRepository.save(cart);
        log.info("Added products to cart for user {}", username);
        return toDto(savedCart);
    }

    @Transactional
    public ShoppingCartDto removeProductsFromCart(String username, List<UUID> productIds) {
        Cart cart = getCartByUsernameOrThrow(username);
        validateCartActive(cart);

        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));
        Cart savedCart = cartRepository.save(cart);
        log.info("Removed products from cart for user {}", username);
        return toDto(savedCart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        Cart cart = getCartByUsernameOrThrow(username);
        validateCartActive(cart);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        if (request.getNewQuantity() <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getNewQuantity());
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Changed product quantity for user {}", username);
        return toDto(savedCart);
    }

    @Transactional
    public void deactivateCart(String username) {
        Cart cart = getCartByUsernameOrThrow(username);
        cart.setActive(false);
        cartRepository.save(cart);
        log.info("Deactivated cart for user {}", username);
    }

    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .username(username)
                            .active(true)
                            .build();
                    log.info("Created new cart for user {}", username);
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartByUsernameOrThrow(String username) {
        return cartRepository.findByUsername(username)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + username));
    }

    private void validateCartActive(Cart cart) {
        if (!cart.isActive()) {
            throw new RuntimeException("Cart is deactivated for user: " + cart.getUsername());
        }
    }

    private ShoppingCartDto toDto(Cart cart) {
        Map<UUID, Integer> products = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(cart.getId());
        dto.setProducts(products);
        return dto;
    }
}