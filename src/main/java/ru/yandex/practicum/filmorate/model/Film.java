package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания фильма — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;


    private Set<Long> likes = new HashSet<>();

    private MpaRating mpa = new MpaRating(null, null);

    private Set<Genre> genres = new LinkedHashSet<>();

    private Set<Director> directors = new LinkedHashSet<>();

}