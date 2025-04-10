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
}
