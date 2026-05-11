package com.example.movietheater.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private String genreName;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private String status;
}