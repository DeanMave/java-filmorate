package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(final ValidationException e) {
        log.warn("Ошибка при валидации данных: {}", e.getMessage());
        return Map.of("error", "Ошибка при валидации данных.", "message", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        log.warn("Ошибка искомый объект не найден: {}", e.getMessage());
        return Map.of("error", "Искомый объект не найден.", "message", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleRuntimeException(final RuntimeException e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return Map.of("error", "Возникло исключение.", "message", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Ошибка внешнего ключа или ограничения целостности: {}", e.getMessage(), e);
        return Map.of("error", "Ошибка базы данных.", "message", e.getMessage());
    }
}
