package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.NotBefore;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    @NotBlank(message = "Название фильма не должно быть пустым")
    private String name;
    @Size(min = 1, max = 200, message = "Максимальная длина описания фильма — 200 символов")
    private String description;
    @NotNull(message = "Дата релиза фильма не должна быть пустой")
    @NotBefore("1895-12-28")
    private LocalDate releaseDate;
    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
    @JsonProperty("mpa")
    private Rating rating;
    private Set<Genre> genres = new HashSet<>();
    private Set<Integer> likes = new HashSet<>();
}
