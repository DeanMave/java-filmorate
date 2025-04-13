package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.RecommendationStorage;

import java.util.List;

@Service
public class RecommendationService {
    private final RecommendationStorage recommendationStorage;
    private final UserService userService;

    public RecommendationService(RecommendationStorage recommendationStorage, UserService userService) {
        this.recommendationStorage = recommendationStorage;
        this.userService = userService;
    }

    public List<Film> getRecommendedFilms(Integer userId) {
        userService.findUser(userId); // Проверка существования пользователя
        return recommendationStorage.getRecommendedFilms(userId);
    }
}
