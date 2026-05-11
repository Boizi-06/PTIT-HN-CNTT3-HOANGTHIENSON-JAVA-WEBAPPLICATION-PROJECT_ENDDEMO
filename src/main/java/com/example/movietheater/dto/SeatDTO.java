package com.example.movietheater.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeatDTO {
    private Long id;
    private String seatName;
    private String seatType;
    private boolean isAvailable;
}