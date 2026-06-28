package ru.yandex.practicum.warehous.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.api.exception.ErrorType;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAlreadyExists(SpecifiedProductAlreadyInWarehouseException e) {
        log.error("Product already in warehouse: {}", e.getMessage());
        return new ErrorResponse(ErrorType.PRODUCT_ALREADY_IN_WAREHOUSE, e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFound(NoSpecifiedProductInWarehouseException e) {
        log.error("Product not found in warehouse: {}", e.getMessage());
        return new ErrorResponse(ErrorType.PRODUCT_NOT_IN_WAREHOUSE, e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleLowQuantity(ProductInShoppingCartLowQuantityInWarehouse e) {
        log.error("Low quantity in warehouse: {}", e.getMessage());
        return new ErrorResponse(ErrorType.LOW_QUANTITY_IN_WAREHOUSE, e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return new ErrorResponse(ErrorType.INTERNAL_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}