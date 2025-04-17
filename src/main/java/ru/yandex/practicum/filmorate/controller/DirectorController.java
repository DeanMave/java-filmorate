package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;


@RestController
@RequestMapping("/directors")
@Validated
public class DirectorController {
    private final DirectorDbStorage directorStorage;
    private final DirectorService directorService;

    public DirectorController(DirectorDbStorage directorStorage, DirectorService directorService) {
        this.directorStorage = directorStorage;
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable @Positive Integer id) {
        return directorService.getDirectorByIdOrThrow(id);
    }

    @PostMapping
    public Director addNewDirector(@Valid @RequestBody Director director) {
        return directorStorage.addNewDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorStorage.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        directorStorage.deleteDirector(id);
    }
}
