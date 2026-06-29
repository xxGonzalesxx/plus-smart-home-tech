package ru.yandex.practicum.order.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.api.exception.ErrorType;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private ErrorType error;
    private String message;
    private int status;
}