package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    List<Film> getAllFilms();

    List<Film> getFilmsByDirectorSortedByLikes(Integer directorId);

    List<Film> getFilmsByDirectorSortedByYears(Integer directorId);

    Film addNewFilm(Film film);

    Film updateFilm(Film newFilm);

    Optional<Film> findById(Integer id);

    List<Film> findMostPopularFilms();

    void addLike(Integer filmId,Integer userID);

    void deleteLike(Integer filmId,Integer userID);

    Set<Integer> getLikesByFilmId(Integer filmId);

    Set<Genre> getGenresByFilmId(Integer filmId);

    Set<Director> getDirectorsByFilmId(Integer filmId);

    public List<Film> getCommonFilmsWithFriend(Integer userId, Integer friendId);
}
