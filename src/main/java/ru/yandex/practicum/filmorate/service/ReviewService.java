package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfaces.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Review addReview(Review review) {
        userService.findUser(review.getUserId());
        filmService.getFilmByIdOrThrow(review.getFilmId());
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        getReviewOrThrow(review.getId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Integer id) {
        getReviewOrThrow(id);
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(Integer id) {
        return getReviewOrThrow(id);
    }

    public List<Review> getFilmReviews(Integer filmId) {
        filmService.getFilmByIdOrThrow(filmId);
        return reviewStorage.getFilmReviews(filmId);
    }

    public List<Review> getUserReviews(Integer userId) {
        userService.findUser(userId);
        return reviewStorage.getUserReviews(userId);
    }

    private Review getReviewOrThrow(Integer id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + id + " не найден"));
    }
}
