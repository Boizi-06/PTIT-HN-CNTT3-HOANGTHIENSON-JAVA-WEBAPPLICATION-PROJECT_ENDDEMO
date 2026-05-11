package com.example.movietheater.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private int duration;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;        // ← Phải có dòng này

    private String trailerUrl;
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private boolean active = true;
}