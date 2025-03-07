package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validators.NotBeforeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBeforeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBefore {
    String message() default "Дата релиза фильма должна быть не раньше 28 декабря 1895 года";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value();
}