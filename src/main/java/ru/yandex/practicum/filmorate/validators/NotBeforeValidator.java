package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotations.NotBefore;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class NotBeforeValidator implements ConstraintValidator<NotBefore, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(NotBefore constraintAnnotation) {
        try {
            minDate = LocalDate.parse(constraintAnnotation.value());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты в аннотации @NotBefore");
        }
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value != null && !value.isBefore(minDate);
    }
}