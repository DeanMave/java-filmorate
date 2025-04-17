package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 300)
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;

    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotNull(message = "Film ID cannot be null")
    private Integer filmId;

    private Integer useful;
}
