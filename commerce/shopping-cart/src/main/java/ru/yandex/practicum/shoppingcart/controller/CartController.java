package ru.yandex.practicum.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.shoppingcart.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingcart.service.CartService;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        log.info("GET /api/v1/shopping-cart - username: {}", username);
        return cartService.getCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Integer> products) {
        log.info("PUT /api/v1/shopping-cart - username: {}, products: {}", username, products);
        return cartService.addProductsToCart(username, products);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds) {
        log.info("POST /api/v1/shopping-cart/remove - username: {}, productIds: {}", username, productIds);
        return cartService.removeProductsFromCart(username, productIds);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(
            @RequestParam String username,
            @RequestBody ChangeProductQuantityRequest request) {
        log.info("POST /api/v1/shopping-cart/change-quantity - username: {}, productId: {}, newQuantity: {}",
                username, request.getProductId(), request.getNewQuantity());
        return cartService.changeProductQuantity(username, request);
    }

    @DeleteMapping
    public void deactivateCurrentShoppingCart(@RequestParam String username) {
        log.info("DELETE /api/v1/shopping-cart - username: {}", username);
        cartService.deactivateCart(username);
    }
}