package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
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
    private final UserService userService;
    private final FilmService filmService;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate,
                           ReviewRowMapper reviewRowMapper,
                           UserService userService,
                           FilmService filmService) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewRowMapper = reviewRowMapper;
        this.userService = userService;
        this.filmService = filmService;
    }

    @Override
    public Review addReview(Review review) {
        userService.findUser(review.getUserId());
        filmService.getFilmByIdOrThrow(review.getFilmId());

        String sql = "INSERT INTO reviews (film_id, user_id, title, content) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setInt(1, review.getFilmId());
            ps.setInt(2, review.getUserId());
            ps.setString(3, review.getTitle());
            ps.setString(4, review.getContent());
            return ps;
        }, keyHolder);

        review.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET title = ?, content = ? WHERE review_id = ?";
        int updated = jdbcTemplate.update(sql,
                review.getTitle(),
                review.getContent(),
                review.getId());

        if (updated == 0) {
            throw new NotFoundException("Отзыв с ID " + review.getId() + " не найден");
        }
        return review;
    }

    @Override
    public void deleteReview(Integer id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Review> getReviewById(Integer id) {
        String sql = "SELECT " +
                "review_id, " +
                "film_id, " +
                "user_id, " +
                "title, " +
                "content " +
                "FROM reviews WHERE review_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reviewRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getFilmReviews(Integer filmId) {
        String sql = "SELECT " +
                "review_id, " +
                "film_id, " +
                "user_id, " +
                "title, " +
                "content " +
                "FROM reviews WHERE film_id = ?";
        return jdbcTemplate.query(sql, reviewRowMapper, filmId);
    }

    @Override
    public List<Review> getUserReviews(Integer userId) {
        String sql = "SELECT " +
                "review_id, " +
                "film_id, " +
                "user_id, " +
                "title, " +
                "content " +
                "FROM reviews WHERE user_id = ?";
        return jdbcTemplate.query(sql, reviewRowMapper, userId);
    }
}
