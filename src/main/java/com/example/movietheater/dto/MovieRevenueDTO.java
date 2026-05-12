package com.example.movietheater.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieRevenueDTO {
    private Long movieId;
    private String movieTitle;
    private Double revenue;
}