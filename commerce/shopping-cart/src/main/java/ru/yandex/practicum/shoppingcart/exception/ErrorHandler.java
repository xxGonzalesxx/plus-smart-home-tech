package ru.yandex.practicum.shoppingcart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.api.exception.ErrorType;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(CartNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCartNotFound(CartNotFoundException e) {
        log.error("Cart not found: {}", e.getMessage());
        return new ErrorResponse(ErrorType.CART_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleNotAuthorized(NotAuthorizedUserException e) {
        log.error("Not authorized: {}", e.getMessage());
        return new ErrorResponse(ErrorType.NOT_AUTHORIZED, e.getMessage(), HttpStatus.UNAUTHORIZED.value());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoProducts(NoProductsInShoppingCartException e) {
        log.error("No products in cart: {}", e.getMessage());
        return new ErrorResponse(ErrorType.NO_PRODUCTS_IN_CART, e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return new ErrorResponse(ErrorType.INTERNAL_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}