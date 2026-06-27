package ru.yandex.practicum.payment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(PaymentNotFoundException e) {
        log.error("Payment not found: {}", e.getMessage());
        return new ErrorResponse("PAYMENT_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotEnoughInfo(NotEnoughInfoInOrderToCalculateException e) {
        log.error("Not enough info: {}", e.getMessage());
        return new ErrorResponse("NOT_ENOUGH_INFO", e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return new ErrorResponse("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}