package ru.yandex.practicum.warehous.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAlreadyExists(SpecifiedProductAlreadyInWarehouseException e) {
        log.error("Product already in warehouse: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNotFound(NoSpecifiedProductInWarehouseException e) {
        log.error("Product not found in warehouse: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleLowQuantity(ProductInShoppingCartLowQuantityInWarehouse e) {
        log.error("Low quantity in warehouse: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return Map.of("error", "Internal server error");
    }
}