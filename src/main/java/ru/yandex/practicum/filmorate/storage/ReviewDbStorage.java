package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfaces.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate,
                           ReviewRowMapper reviewRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewRowMapper = reviewRowMapper;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int updated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (updated == 0) {
            throw new NotFoundException("Отзыв с ID " + review.getReviewId() + " не найден");
        }
        return review;
    }

    @Override
    public void deleteReview(Integer id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Review> getFilmReviews(Integer filmId, Integer count) {
        String sql = "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, " +
                "(SELECT COUNT(*) FROM review_likes WHERE review_id = r.review_id) - " +
                "(SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.review_id) AS useful " +
                "FROM reviews r " +
                "WHERE r.film_id = ? LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
    }

    @Override
    public List<Review> getUserReviews(Integer userId) {
        String sql = "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, "
                + "(COUNT(DISTINCT rl.user_id) - COUNT(DISTINCT rd.user_id)) AS useful "
                + "FROM reviews r "
                + "LEFT JOIN review_likes rl ON r.review_id = rl.review_id "
                + "LEFT JOIN review_dislikes rd ON r.review_id = rd.review_id "
                + "WHERE r.user_id = ? " // Исправлено film_id на user_id
                + "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive";

        return jdbcTemplate.query(sql, reviewRowMapper, userId);
    }

    @Override
    public void addLike(Integer reviewId, Integer userId) {
        String sql = "MERGE INTO review_likes (review_id, user_id) KEY(review_id, user_id) VALUES (?, ?)";
        if (existsDislike(reviewId, userId)) {
            removeLike(reviewId, userId);
        }
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public void removeLike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public Review addReview(Review review) {
        String sql = "INSERT INTO reviews (film_id, user_id, content, is_positive) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setInt(1, review.getFilmId());
            ps.setInt(2, review.getUserId());
            ps.setString(3, review.getContent());
            ps.setBoolean(4, review.getIsPositive());
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return review;
    }

    @Override
    public Optional<Review> getReviewById(Integer id) {
        String sql = "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, "
                + "(SELECT COUNT(*) FROM review_likes WHERE review_id = r.review_id) - "
                + "(SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.review_id) AS useful "
                + "FROM reviews r "
                + "WHERE r.review_id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reviewRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sql = "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, "
                + "(SELECT COUNT(*) FROM review_likes WHERE review_id = r.review_id)  - "
                + "(SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.review_id) AS useful "
                + "FROM reviews r "
                + "ORDER BY useful DESC "
                + "LIMIT ?";

        return jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    @Override
    @Transactional
    public void addDislike(Integer reviewId, Integer userId) {
        String sql = "MERGE INTO review_dislikes (review_id, user_id) KEY(review_id, user_id) VALUES (?, ?)";
        if (existsLike(reviewId, userId)) {
            removeLike(reviewId, userId);
        }
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public void removeDislike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM review_dislikes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    private boolean existsLike(Integer reviewId, Integer userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM review_likes WHERE review_id = ? AND user_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                reviewId,
                userId
        ));
    }

    private boolean existsDislike(Integer reviewId, Integer userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM review_dislikes WHERE review_id = ? AND user_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                reviewId,
                userId
        ));
    }
}
