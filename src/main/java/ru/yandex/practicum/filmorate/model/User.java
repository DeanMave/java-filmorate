package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;
    private String name;
    private String login;
    private String email;
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
