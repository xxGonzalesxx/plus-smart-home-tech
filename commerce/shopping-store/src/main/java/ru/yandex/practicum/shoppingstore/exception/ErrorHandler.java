package ru.yandex.practicum.shoppingstore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.api.exception.ErrorType;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ProductNotFoundException e) {
        log.error("Product not found: {}", e.getMessage());
        return new ErrorResponse(ErrorType.PRODUCT_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return new ErrorResponse(ErrorType.INTERNAL_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}