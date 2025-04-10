package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        Review existing = getReviewOrThrow(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        return reviewStorage.updateReview(existing);
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

    private void validateReview(Review review) {
        if (review.getIsPositive() == null) {
            throw new ValidationException("Field 'isPositive' is required");
        }
        userService.findUser(review.getUserId());
        filmService.getFilmByIdOrThrow(review.getFilmId());
    }

    public void addLike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        userService.findUser(userId);
        reviewStorage.addLike(reviewId, userId);
        reviewStorage.updateReview(review);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        userService.findUser(userId);
        reviewStorage.removeLike(reviewId, userId);
        reviewStorage.updateReview(review);
    }

    public List<Review> getAllReviews(int count) {
        if (count <= 0) {
            throw new ValidationException("Count должен быть положительным числом");
        }
        return reviewStorage.getAllReviews(count);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        getReviewOrThrow(reviewId);
        userService.findUser(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        userService.findUser(userId);
        reviewStorage.removeDislike(reviewId, userId);
        reviewStorage.updateReview(review);
    }
}
