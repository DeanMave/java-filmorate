package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;
    private String name;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    private String login;
    @Email(message = "Некорректная электронная почта")
    @NotBlank(message = "Электронная почта не должна быть пустой")
    private String email;
    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
