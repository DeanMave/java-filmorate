package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Integer id);

    Optional<Review> getReviewById(Integer id);

    List<Review> getFilmReviews(Integer filmId);

    List<Review> getUserReviews(Integer userId);

    void removeLike(Integer reviewId, Integer userId);

    void addLike(Integer reviewId, Integer userId);

    List<Review> getAllReviews(int count);

    void addDislike(Integer reviewId, Integer userId);

    void removeDislike(Integer reviewId, Integer userId);
}
