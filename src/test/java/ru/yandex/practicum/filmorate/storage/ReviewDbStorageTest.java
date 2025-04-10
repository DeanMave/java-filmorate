package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ReviewDbStorage.class, ReviewRowMapper.class, FilmDbStorage.class,
        UserDbStorage.class, FilmRowMapper.class, GenreRowMapper.class, UserRowMapper.class, UserService.class,
        FilmService.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {
    private final ReviewDbStorage reviewDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        Rating rating = new Rating();
        rating.setId(1);
        testFilm.setRating(rating);
        testFilm = filmDbStorage.addNewFilm(testFilm);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testLogin");
        testUser.setEmail("test@mail.com");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser = userDbStorage.create(testUser);
    }

    @Test
    void testCreateAndFindReviewById() {
        Review review = new Review();
        review.setUserId(testUser.getId());
        review.setFilmId(testFilm.getId());
        review.setTitle("Great film");
        review.setContent("Really enjoyed watching it!");

        Review created = reviewDbStorage.addReview(review);

        Optional<Review> optionalReview = reviewDbStorage.getReviewById(created.getId());

        assertThat(optionalReview)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(r).hasFieldOrPropertyWithValue("title", "Great film");
                    assertThat(r).hasFieldOrPropertyWithValue("content", "Really enjoyed watching it!");
                    assertThat(r.getUserId()).isEqualTo(testUser.getId());
                    assertThat(r.getFilmId()).isEqualTo(testFilm.getId());
                });
    }

    @Test
    void testUpdateReview() {
        Review review = new Review();
        review.setUserId(testUser.getId());
        review.setFilmId(testFilm.getId());
        review.setTitle("Initial title");
        review.setContent("Initial content");
        Review created = reviewDbStorage.addReview(review);

        created.setTitle("Updated title");
        created.setContent("Updated content");
        Review updated = reviewDbStorage.updateReview(created);

        Optional<Review> optionalReview = reviewDbStorage.getReviewById(updated.getId());

        assertThat(optionalReview)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getTitle()).isEqualTo("Updated title");
                    assertThat(r.getContent()).isEqualTo("Updated content");
                });
    }

    @Test
    void testDeleteReview() {
        Review review = new Review();
        review.setUserId(testUser.getId());
        review.setFilmId(testFilm.getId());
        review.setTitle("To delete");
        review.setContent("Delete me");
        Review created = reviewDbStorage.addReview(review);

        reviewDbStorage.deleteReview(created.getId());

        Optional<Review> deleted = reviewDbStorage.getReviewById(created.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void testGetFilmReviews() {
        Review review1 = new Review();
        review1.setUserId(testUser.getId());
        review1.setFilmId(testFilm.getId());
        review1.setTitle("Review 1");
        review1.setContent("Content 1");
        reviewDbStorage.addReview(review1);

        Review review2 = new Review();
        review2.setUserId(testUser.getId());
        review2.setFilmId(testFilm.getId());
        review2.setTitle("Review 2");
        review2.setContent("Content 2");
        reviewDbStorage.addReview(review2);

        List<Review> filmReviews = reviewDbStorage.getFilmReviews(testFilm.getId());

        assertThat(filmReviews)
                .hasSize(2)
                .extracting(Review::getTitle)
                .containsExactlyInAnyOrder("Review 1", "Review 2");
    }

    @Test
    void testGetUserReviews() {
        Review review1 = new Review();
        review1.setUserId(testUser.getId());
        review1.setFilmId(testFilm.getId());
        review1.setTitle("User Review 1");
        review1.setContent("User Content 1");
        reviewDbStorage.addReview(review1);

        Review review2 = new Review();
        review2.setUserId(testUser.getId());
        review2.setFilmId(testFilm.getId());
        review2.setTitle("User Review 2");
        review2.setContent("User Content 2");
        reviewDbStorage.addReview(review2);

        List<Review> userReviews = reviewDbStorage.getUserReviews(testUser.getId());

        assertThat(userReviews)
                .hasSize(2)
                .extracting(Review::getTitle)
                .containsExactlyInAnyOrder("User Review 1", "User Review 2");
    }

    @Test
    void testUpdateNonExistingReviewShouldThrow() {
        Review review = new Review();
        review.setId(9999);
        review.setTitle("Non existing");
        review.setContent("Should throw");

        assertThatThrownBy(() -> reviewDbStorage.updateReview(review))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Отзыв с ID 9999 не найден");
    }
}
