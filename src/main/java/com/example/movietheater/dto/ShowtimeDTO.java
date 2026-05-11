package com.example.movietheater.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShowtimeDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private Double price;
    private String status;
}