package ru.yandex.practicum.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(OrderNotFoundException e) {
        log.error("Order not found: {}", e.getMessage());
        return new ErrorResponse("ORDER_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(NoOrderFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoOrder(NoOrderFoundException e) {
        log.error("Order error: {}", e.getMessage());
        return new ErrorResponse("ORDER_ERROR", e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return new ErrorResponse("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}