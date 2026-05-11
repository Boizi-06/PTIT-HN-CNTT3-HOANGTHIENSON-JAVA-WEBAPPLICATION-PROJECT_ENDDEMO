package com.example.movietheater.entity;

import com.example.movietheater.eenum.SeatStatus;
import com.example.movietheater.eenum.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatName;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    // THÊM MỚI
    @Enumerated(EnumType.STRING)
    private SeatType seatType = SeatType.STANDARD;
}